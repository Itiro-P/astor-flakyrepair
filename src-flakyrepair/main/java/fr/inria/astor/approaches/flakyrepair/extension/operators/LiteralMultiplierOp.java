package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.ExpresionMutOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

public class LiteralMultiplierOp extends ExpresionMutOp {
	private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("sleep", "wait", "join", "emit", "pause"));
	private static final List<Double> multipliers = Arrays.asList(0.1, 0.5, 1.25, 2.0, 3.0, 4.0);
	public LiteralMultiplierOp() {
		super();
	}
	
	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement el = point.getCodeElement();
		return el instanceof CtLiteral && 
			   el.getParent() instanceof CtInvocation && 
			   allowedMethods.contains(((CtInvocation<?>) el.getParent()).getExecutable().getSimpleName());
	}

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
			throws IllegalAccessException {
		CtLiteral<?> targetLiteral = (CtLiteral<?>) point.getCodeElement();
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(targetLiteral);
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement());

		return operation;
	}

	/** Return the list of CtElements Mutanted */
	@Override
	public List<MutantCtElement> getMutants(CtElement element) {
		CtLiteral<?> literal = (CtLiteral<?>) element;
		List<MutantCtElement> mutations = new ArrayList<>();
		if (literal.getValue() instanceof Number) {
			Number value = (Number) literal.getValue();
			for (Double multiplier : multipliers) {
				// Multiply by each multiplier
				Number newValue = value.doubleValue() * multiplier;
				CtLiteral mutant = literal.clone();
				mutant.setValue(newValue);
				CtComment comment = mutant.getFactory().Code().createComment("Replaced " + value + " with " + newValue, CtComment.CommentType.INLINE);
				mutant.addComment(comment);
				mutations.add(new MutantCtElement(mutant, 1.0));
			}
		}
		return mutations;
	}

	@Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}
}
