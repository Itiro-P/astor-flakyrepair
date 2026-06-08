package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.ShuffleJSONMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.ShuffleListMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.ShuffleMapMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.ShuffleSetMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador unificado que agrega os diversos mutators de "shuffle" (List, Set, Map, JSON)
 * e pode ser aplicado nos mesmos pontos onde os operadores específicos aplicavam.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ShuffleCollectionOp extends AutonomousOperator {

    MutatorComposite mutatorComposite = null;

    public ShuffleCollectionOp() {
        super();
        this.mutatorComposite = new MutatorComposite(MutationSupporter.getFactory());
        // adiciona todos os mutators existentes sem excluir os arquivos originais
        this.mutatorComposite.getMutators().add(new ShuffleListMutator(this.mutatorComposite.getFactory()));
        this.mutatorComposite.getMutators().add(new ShuffleSetMutator(this.mutatorComposite.getFactory()));
        this.mutatorComposite.getMutators().add(new ShuffleMapMutator(this.mutatorComposite.getFactory()));
        this.mutatorComposite.getMutators().add(new ShuffleJSONMutator(this.mutatorComposite.getFactory()));
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();

        // Caso: invocação de assert com uma List entre os argumentos (como em ShuffleListOp)
        if (element instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) element;
            String methodName = invocation.getExecutable().getSimpleName().toLowerCase();
            if (methodName.startsWith("assert")) {
                List<CtExpression> args = invocation.getArguments();
                return args.stream()
                        .filter(arg -> arg instanceof CtVariableRead)
                        .anyMatch(arg -> isList(arg.getType()));
            }
            return false;
        }

        // Caso: variável local ou construção, checar tipos Map/Set/JSON/List
        if (element instanceof CtLocalVariable) {
            CtLocalVariable el = (CtLocalVariable) element;
            CtTypeReference<?> type = el.getAssignment() != null ? el.getAssignment().getType() : el.getType();
            return isList(type) || isSet(type) || isMap(type) || isJSON(type);

        } else if (element instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) element;
            CtTypeReference<?> type = ctc.getType();
            return isList(type) || isSet(type) || isMap(type) || isJSON(type);
        }

        return false;
    }

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        boolean successful = false;
        try {
            CtElement ctst = operation.getOriginal();
            CtElement fix = operation.getModified();

            ctst.replace(fix);
            successful = true;
            operation.setSuccessfulyApplied((successful));

            log.debug(" applied: " + ctst.getParent().toString());

        } catch (Exception ex) {
            log.error("Error applying an operation, exception: " + ex.getMessage());
            operation.setExceptionAtApplied(ex);
            operation.setSuccessfulyApplied(false);
        }
        return successful;
    }

    @Override
    public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {
        List<OperatorInstance> ops = new ArrayList<>();

        List<MutantCtElement> mutations = getMutants(modificationPoint.getCodeElement());
        for (MutantCtElement mutantCtElement : mutations) {
            OperatorInstance opInstance;
            try {
                opInstance = createModificationInstance(modificationPoint, mutantCtElement);
                if (opInstance != null)
                    ops.add(opInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return ops;
    }

    @Override
    public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
        try {
            CtElement ctst = opInstance.getOriginal();
            CtElement fix = opInstance.getModified();
            fix.replace(ctst);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isList(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(java.util.List.class));
    }

    private boolean isSet(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(java.util.Set.class));
    }

    private boolean isMap(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(java.util.Map.class));
    }

    private boolean isJSON(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(org.json.simple.JSONObject.class));
    }

    public List<MutantCtElement> getMutants(CtElement element) {
        return this.mutatorComposite.execute(element);
    }

    protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
            throws IllegalAccessException {

        CtElement element = point.getCodeElement();

        CtElement originalForOperation = element;

        // se o mutator gerou um CtBlock (como em ShuffleListMutator), usamos o bloco pai como original
        if (fix.getElement() instanceof CtBlock) {
            CtBlock target = element instanceof CtBlock ? (CtBlock) element : element.getParent(CtBlock.class);
            if (target == null) {
                log.error("Não foi possível encontrar um CtBlock pai para o elemento: " + element.getClass());
                return null;
            }
            originalForOperation = target;
        }

        OperatorInstance operation = new OperatorInstance();
        operation.setOriginal(originalForOperation);
        operation.setModified(fix.getElement());
        operation.setOperationApplied(this);
        operation.setModificationPoint(point);
        return operation;
    }

    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        return false;
    }

}
