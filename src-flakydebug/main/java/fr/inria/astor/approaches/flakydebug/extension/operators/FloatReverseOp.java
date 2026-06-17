package fr.inria.astor.approaches.flakydebug.extension.operators;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;

/**
 * Mutator que troca operandos Float de lado. Isso pode levar a erros de imprecisão que (talvez) não foram considerados.
 * (Até agora), não há um PR que demonstre esta instabilidade. Mas o IEEE 754 não garante associatividade.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("unchecked")
public class FloatReverseOp extends Operator {
	public FloatReverseOp() {
		super();
        this.mutatorComposite.getMutators().add(new FloatReverseMutator(this.mutatorComposite.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		// A operação deve ser de adição.
		if(((CtBinaryOperator<?>) element).getKind() != BinaryOperatorKind.PLUS) return false;
		// Vemos se é um opareando.
		return (element instanceof CtBinaryOperator);
	}
}