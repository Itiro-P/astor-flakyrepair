package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

@SuppressWarnings({"rawtypes", "unchecked"})
/**
 * Mutator que injeta flakiness ordenando coleções aleatoriamente antes da primeira asserção.
 * @author Pedro Itiro Nagao
 */
public class ShuffleCollectionMutator extends SpoonMutator<CtBlock> {
    private Set<String> collections = new HashSet<>();

    public ShuffleCollectionMutator(Factory factory) {
        super(factory);
        this.collections.add("add");
        this.collections.add("remove");
        this.collections.add("size");
        this.collections.add("iterator");
        this.collections.add("contains");
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        
        // toMutate aqui é o CtInvocation (o assert) identificado no canBeAppliedToPoint
        if (!(toMutate instanceof CtInvocation)) return result;
        CtInvocation assertion = (CtInvocation) toMutate;
        
        // Buscamos o bloco pai para poder clonar e modificar
        CtBlock parentBlock = assertion.getParent(CtBlock.class);
        if (parentBlock == null) return result;

        // Analisamos os argumentos do assert para saber quem embaralhar
        List<CtExpression> args = assertion.getArguments();
        
        for (CtExpression arg : args) {
            if (arg instanceof CtVariableRead && isCollection(arg.getType())) {
                CtVariableRead varRead = (CtVariableRead) arg;
                
                // Criamos o clone do bloco para a variante
                CtBlock mutatedBlock = parentBlock.clone();
                
                // Localizamos o assert equivalente dentro do clone
                CtInvocation assertInClone = mutatedBlock.getElements(
                    new TypeFilter<CtInvocation>(CtInvocation.class))
                    .stream()
                    .filter(inv -> inv.equals(assertion))
                    .findFirst()
                    .orElse(null);

                if (assertInClone != null) {
                    // Criamos a chamada Collections.shuffle(var)
                    CtInvocation shuffleCall = createShuffleCall(varRead.getVariable());
                    
                    // Injetamos ANTES do assert
                    assertInClone.insertBefore(shuffleCall);
                    result.add(new MutantCtElement(mutatedBlock, 1.0));
                }
            }
        }
        return result;
    }

    private CtInvocation createShuffleCall(CtVariableReference varRef) {
        CtTypeReference collectionsType = factory.Type().createReference(java.util.Collections.class);
        CtExecutableReference shuffleRef = factory.Executable().createReference(
            collectionsType,
            factory.Type().voidPrimitiveType(),
            "shuffle",
            factory.Type().createReference(java.util.List.class)
        );
        
        return factory.createInvocation(
            factory.createTypeAccess(collectionsType),
            shuffleRef,
            factory.createVariableRead(varRef, false)
        );
    }

    private boolean isCollection(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        try {
            CtType<?> typeDecl = typeRef.getTypeDeclaration();
            if (typeDecl == null) return false;
            return typeDecl.getAllMethods().stream()
                .map(m -> m.getSimpleName())
                .anyMatch(this.collections::contains);
        } catch (Exception e) {
            String name = typeRef.getQualifiedName();
            return name.contains("List") || name.contains("Collection") || name.contains("Set");
        }
    }

    @Override
    public String key() { return "shuffleCollectionMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}