package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.LiteralMultiplierMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

/**
 * Operator que multiplica literais numéricos de certos métodos por um fator (ex: 2x). 
 * Útil para lidar com testes flaky causados por valores limite ou condições de corrida que dependem de tempos ou contagens específicas.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class LiteralMultiplierOp extends Operator {
	private Set<String> allowedMethods = new HashSet<>(Arrays.asList("sleep", "wait", "join", "countDown", "incrementAndGet", "decrementAndGet"));

	public LiteralMultiplierOp() {
		super();

        this.mutatorComposite.getMutators().add(new LiteralMultiplierMutator(this.mutatorComposite.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		// Vemos se é um literal.
		if (!(element instanceof CtLiteral)) return false;
        CtLiteral literal = (CtLiteral) element;
		// Agora vemos se é uma invocaćão e é um dos métodos mutáveis.
		return literal.getParent() instanceof CtInvocation
            && allowedMethods.contains(((CtInvocation) literal.getParent()).getExecutable().getSimpleName());
	}
}