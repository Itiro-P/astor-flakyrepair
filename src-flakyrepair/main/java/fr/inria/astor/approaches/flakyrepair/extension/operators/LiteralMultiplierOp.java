package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.ExpresionMutOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

public class LiteralMultiplierOp extends ExpresionMutOp {

	public LiteralMultiplierOp() {
		super();
	}
	
	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement el = point.getCodeElement();
		return el instanceof CtLiteral && el.getParent() instanceof CtInvocation;
	}

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
			throws IllegalAccessException {
		CtLiteral targetLiteral = (CtLiteral) point.getCodeElement();
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
		CtLiteral literal = (CtLiteral) element;
		List<MutantCtElement> mutations = new ArrayList<>();
		if (literal.getValue() instanceof Number) {
			Number value = (Number) literal.getValue();
			// Multiply by 2 as example
			Number newValue = value.longValue() * 2;
			CtLiteral mutant = literal.clone();
			mutant.setValue(newValue);
			mutations.add(new MutantCtElement(mutant, 1.0));
		}
		return mutations;
	}

	@Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}
}
