package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

@SuppressWarnings({"rawtypes", "unchecked"})
/**
 * Mutator que injeta flakiness ordenando listas aleatoriamente antes da primeira asserção.
 * @author Pedro Itiro Nagao
 */
public class ShuffleListMutator extends SpoonMutator<CtBlock> {

    public ShuffleListMutator(Factory factory) {
        super(factory);
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
            if (arg instanceof CtVariableRead) {
                CtVariableRead varRead = (CtVariableRead) arg;
                CtTypeReference<?> varDeclType = varRead.getVariable() != null ? varRead.getVariable().getType() : null;
                if (!isList(arg.getType()) && (varDeclType == null || !isList(varDeclType))) {
                    continue;
                }
                
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
                break;
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

    private boolean isList(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(java.util.List.class));
    }

    @Override
    public String key() { return "shuffleListMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}