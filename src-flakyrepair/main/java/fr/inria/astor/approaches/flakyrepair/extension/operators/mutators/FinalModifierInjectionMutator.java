package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;

/**
 * Mutator que adiciona `final` a métodos não-final.
 *
 * Clona o `CtMethod` alvo e adiciona o modificador `final`, retornando
 * a versão mutada como um `MutantCtElement`.
 */
public class FinalModifierInjectionMutator extends SpoonMutator<CtMethod> {
    public FinalModifierInjectionMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtMethod)) return result;
        CtMethod method = (CtMethod) toMutate;
        
        // Se já for final, não faz sentido mutar
        if (method.hasModifier(ModifierKind.FINAL)) {
            return result;
        }
        // Clona o método e injeta o modificador `final` na cópia.
        CtMethod mutatedMethod = factory.Core().clone(method);
        mutatedMethod.addModifier(ModifierKind.FINAL);
        
        result.add(new MutantCtElement(mutatedMethod, 1));
        return result;
    }

    @Override
	public String key() {
		return "finalModifierInjectionOp";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}
