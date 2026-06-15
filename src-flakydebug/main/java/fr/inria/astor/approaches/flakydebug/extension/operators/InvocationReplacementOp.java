package fr.inria.astor.approaches.flakydebug.extension.operators;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.MethodConstraintMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtInvocation;

/**
 * Operator que substitui invocações por variantes mais restritivas (ex.:
 * métodos que exigem ordem).
 * @author Pedro Itiro Nagao
 *
 */
@SuppressWarnings("unchecked")
public class InvocationReplacementOp extends Operator {
	public InvocationReplacementOp() {
		super();

        this.mutatorComposite.getMutators().add(new MethodConstraintMutator(this.mutatorComposite.getFactory()));
	}

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
		// Aplica apenas em pontos que sejam invocações (chamadas de método).
		return (point.getCodeElement() instanceof CtInvocation);
    }
}
