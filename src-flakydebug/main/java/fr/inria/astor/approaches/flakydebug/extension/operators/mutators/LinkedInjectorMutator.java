package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Operador de mutação que injeta embaralhamento em coleções imediatamente antes
 * de asserções, expondo comportamentos flaky causados por dependência de ordem
 * de iteração.
 *
 * <p>A estratégia, para uma variável {@code col} passada a um assert:</p>
 * <ol>
 *   <li>Declara {@code List _tmp = new ArrayList(col.entrySet())} (Map) ou
 *       {@code new ArrayList(col)} (Set).</li>
 *   <li>Chama {@code Collections.shuffle(_tmp)}.</li>
 *   <li>Reatribui {@code col} via stream:
 *     <ul>
 *       <li>Map: {@code _tmp.stream().collect(toMap(e->e.getKey(), e->e.getValue(), (a,b)->a, LinkedHashMap::new))}</li>
 *       <li>Set: {@code _tmp.stream().collect(toCollection(LinkedHashSet::new))}</li>
 *     </ul>
 *   </li>
 * </ol>
 * <p>Tudo construído via API Spoon — sem nenhum {@code CtCodeSnippetStatement}.</p>
 *
 * <p>Motivação em projetos reais:</p>
 * <ul>
 *   <li>Druid: <a href="https://github.com/alibaba/druid/pull/4717">PR #4717</a> — HashMap → LinkedHashMap</li>
 *   <li>IoTDB: <a href="https://github.com/apache/iotdb/pull/13961">PR #13961</a> — HashSet → LinkedHashSet</li>
 *   <li>Linkis: <a href="https://github.com/apache/linkis/pull/5005">PR #5005</a> — uso de TreeMap</li>
 * </ul>
 *
 * @author Pedro Itiro Nagao
 */
public class LinkedInjectorMutator extends SpoonMutator<CtInvocation> {

    public LinkedInjectorMutator(Factory factory) {
        super(factory);
    }
    @Override
    @SuppressWarnings("unchecked")
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (!(toMutate instanceof CtInvocation)) return result;
        CtInvocation<?> assertion = (CtInvocation<?>) toMutate;

        CtBlock<?> parentBlock = assertion.getParent(CtBlock.class);
        if (parentBlock == null) return result;

        for (CtExpression<?> arg : assertion.getArguments()) {
            if (!(arg instanceof CtVariableRead)) continue;
            CtVariableRead<?> varRead = (CtVariableRead<?>) arg;
            if (!isShuffleable(varRead.getType())) continue;

            CtBlock<?> mutatedBlock = parentBlock.clone();

            // Localiza o assert no clone por índice para evitar falso-positivo
            // quando o mesmo assert aparece mais de uma vez no bloco.
            int assertIndex = parentBlock
                .getElements(new TypeFilter<>(CtInvocation.class))
                .indexOf(assertion);

            List<CtInvocation<?>> assertsInClone =
                mutatedBlock.getElements(new TypeFilter<>(CtInvocation.class));

            if (assertIndex < 0 || assertIndex >= assertsInClone.size()) continue;

            CtInvocation<?> assertInClone = assertsInClone.get(assertIndex);

            // Encontra a referência da variável dentro do clone pelo mesmo índice de argumento.
            int argIndex = assertion.getArguments().indexOf(arg);
            CtVariableRead<?> varReadInClone =
                (CtVariableRead<?>) assertInClone.getArguments().get(argIndex);

            boolean isMap = isMap(varRead.getType());
            List<CtElement> stmts = isMap
                ? buildMapShuffleStatements(varReadInClone)
                : buildSetShuffleStatements(varReadInClone);

            // Insere os statements antes do assert, na ordem correta.
            for (CtElement stmt : stmts) {
                assertInClone.insertBefore((CtInvocation<?>) stmt);
            }

            result.add(new MutantCtElement(mutatedBlock, 1.0));
        }

        return result;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<CtElement> buildMapShuffleStatements(CtVariableRead<?> varRead) {
        List<CtElement> stmts = new ArrayList<>();

        // 1. col.entrySet()
        CtTypeReference arrayListType = factory.Type().createReference(ArrayList.class);
        CtInvocation<?> entrySetCall = factory.createInvocation(
            varRead.clone(),
            execRef(varRead.getType(), "entrySet")
        );

        // 2. List _tmp = new ArrayList(col.entrySet())
        CtLocalVariable<?> tmpDecl = factory.Code().createLocalVariable(
            factory.Type().createReference(List.class),
            "_tmp",
            factory.createConstructorCall(arrayListType, entrySetCall)
        );
        stmts.add(tmpDecl);

        CtLocalVariableReference tmpRef = tmpDecl.getReference();

        // 3. Collections.shuffle(_tmp)
        stmts.add(buildShuffleCall(tmpRef));

        // 4. _tmp.stream()
        CtInvocation<?> streamCall = factory.createInvocation(
            factory.createVariableRead(tmpRef, false),
            execRef(arrayListType, "stream")
        );

        // 5. Lambdas para Collectors.toMap
        //    e -> e.getKey()
        CtLambda<?> keyExtractor = buildEntryLambda("_e1", "getKey");
        //    e -> e.getValue()
        CtLambda<?> valueExtractor = buildEntryLambda("_e2", "getValue");
        //    (a, b) -> a
        CtLambda<?> mergeFunction = buildMergeLambda();
        //    LinkedHashMap::new  (passado como type access — Spoon trata como supplier)
        CtExpression<?> mapSupplier = factory.createTypeAccess(
            factory.Type().createReference(LinkedHashMap.class));

        // 6. Collectors.toMap(keyExtractor, valueExtractor, mergeFunction, LinkedHashMap::new)
        CtTypeReference collectorsType = factory.Type().createReference(Collectors.class);
        CtInvocation<?> toMapCall = factory.createInvocation(
            factory.createTypeAccess(collectorsType),
            execRef(collectorsType, "toMap"),
            keyExtractor, valueExtractor, mergeFunction, mapSupplier
        );

        // 7. _tmp.stream().collect(toMap(...))
        CtTypeReference streamType =
            factory.Type().createReference(java.util.stream.Stream.class);
        CtInvocation<?> collectCall = factory.createInvocation(
            streamCall,
            execRef(streamType, "collect"),
            toMapCall
        );

        // 8. col = collectCall
        stmts.add(buildReassignment(varRead, collectCall));

        return stmts;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<CtElement> buildSetShuffleStatements(CtVariableRead<?> varRead) {
        List<CtElement> stmts = new ArrayList<>();

        // 1. List _tmp = new ArrayList(col)
        CtTypeReference arrayListType = factory.Type().createReference(ArrayList.class);
        CtLocalVariable<?> tmpDecl = factory.Code().createLocalVariable(
            factory.Type().createReference(List.class),
            "_tmp",
            factory.createConstructorCall(arrayListType, varRead.clone())
        );
        stmts.add(tmpDecl);

        CtLocalVariableReference tmpRef = tmpDecl.getReference();

        // 2. Collections.shuffle(_tmp)
        stmts.add(buildShuffleCall(tmpRef));

        // 3. _tmp.stream()
        CtInvocation<?> streamCall = factory.createInvocation(
            factory.createVariableRead(tmpRef, false),
            execRef(arrayListType, "stream")
        );

        // 4. LinkedHashSet::new como supplier para toCollection
        CtExpression<?> setSupplier = factory.createTypeAccess(
            factory.Type().createReference(LinkedHashSet.class));

        // 5. Collectors.toCollection(LinkedHashSet::new)
        CtTypeReference collectorsType = factory.Type().createReference(Collectors.class);
        CtInvocation<?> toCollectionCall = factory.createInvocation(
            factory.createTypeAccess(collectorsType),
            execRef(collectorsType, "toCollection"),
            setSupplier
        );

        // 6. _tmp.stream().collect(toCollection(...))
        CtTypeReference streamType =
            factory.Type().createReference(java.util.stream.Stream.class);
        CtInvocation<?> collectCall = factory.createInvocation(
            streamCall,
            execRef(streamType, "collect"),
            toCollectionCall
        );

        // 7. col = collectCall
        stmts.add(buildReassignment(varRead, collectCall));

        return stmts;
    }

    /** Constrói {@code Collections.shuffle(listRef)}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CtInvocation<?> buildShuffleCall(CtLocalVariableReference listRef) {
        CtTypeReference collectionsType =
            factory.Type().createReference(Collections.class);
        return factory.createInvocation(
            factory.createTypeAccess(collectionsType),
            execRef(collectionsType, "shuffle"),
            factory.createVariableRead(listRef, false)
        );
    }

    /**
     * Constrói uma lambda de um parâmetro que chama um método sem argumentos
     * sobre ele. Ex: {@code _e1 -> _e1.getKey()}.
     *
     * @param paramName  nome do parâmetro
     * @param methodName método a chamar sobre o parâmetro
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CtLambda buildEntryLambda(String paramName, String methodName) {
        CtLambda lambda = factory.Core().createLambda();

        CtParameter param = factory.Core().createParameter();
        param.setSimpleName(paramName);
        param.setType(factory.Type().objectType());
        lambda.addParameter(param);

        CtTypeReference entryType = factory.Type().createReference(Map.Entry.class);
        CtInvocation<?> body = factory.createInvocation(
            factory.createVariableRead(param.getReference(), false),
            execRef(entryType, methodName)
        );
        lambda.setExpression(body);

        return lambda;
    }

    /**
     * Constrói a lambda de merge {@code (_a, _b) -> _a} usada em
     * {@code Collectors.toMap} para resolver colisões de chave (mantém o primeiro).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CtLambda buildMergeLambda() {
        CtLambda lambda = factory.Core().createLambda();
 
        CtParameter paramA = factory.Core().createParameter();
        paramA.setSimpleName("_a");
        paramA.setType(factory.Type().objectType());
 
        CtParameter paramB = factory.Core().createParameter();
        paramB.setSimpleName("_b");
        paramB.setType(factory.Type().objectType());
 
        lambda.addParameter(paramA);
        lambda.addParameter(paramB);

        lambda.setExpression(
            factory.createVariableRead(paramA.getReference(), false));

        return lambda;
    }

    /** Constrói a reatribuição {@code col = expr}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CtAssignment<?, ?> buildReassignment(CtVariableRead<?> target,
                                                  CtExpression<?> value) {
        CtAssignment assignment = factory.Core().createAssignment();
        assignment.setAssigned(
            factory.createVariableWrite(target.getVariable(), false));
        assignment.setAssignment(value);
        return assignment;
    }

    /**
     * Cria uma referência executável para o método {@code methodName} declarado
     * em {@code declaringType}. Parâmetros opcionais para desambiguação de sobrecarga.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CtExecutableReference execRef(CtTypeReference declaringType,
                                           String methodName,
                                           CtTypeReference... paramTypes) {
        CtExecutableReference ref = factory.Core().createExecutableReference();
        ref.setDeclaringType(declaringType);
        ref.setSimpleName(methodName);
        if (paramTypes.length > 0) {
            List<CtTypeReference<?>> params = new ArrayList<>();
            for (CtTypeReference p : paramTypes) params.add(p);
            ref.setParameters(params);
        }
        return ref;
    }

    private boolean isShuffleable(CtTypeReference<?> typeRef) {
        return isMap(typeRef) || isSet(typeRef);
    }

    private boolean isMap(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        try {
            return typeRef.isSubtypeOf(factory.Type().createReference(Map.class));
        } catch (Exception ignored) {
            return typeRef.getSimpleName().endsWith("Map");
        }
    }

    private boolean isSet(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        try {
            return typeRef.isSubtypeOf(
                factory.Type().createReference(java.util.Set.class));
        } catch (Exception ignored) {
            return typeRef.getSimpleName().endsWith("Set");
        }
    }

    @Override
    public String key() {
        return "linkedInjectorOp";
    }

    @Override
    public int levelMutation() {
        return 1;
    }

    @Override
    public void setup() { /* no-op */ }
}