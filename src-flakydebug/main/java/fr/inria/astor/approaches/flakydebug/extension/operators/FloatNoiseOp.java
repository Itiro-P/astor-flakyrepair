package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatNoiseOp extends AutonomousOperator {
	MutatorComposite mutatorBinary = null;
	final Set<CtTypeReference> types = new HashSet<>(Arrays.asList(
		this.mutatorBinary.getFactory().Type().createReference(java.lang.Float.class),
		this.mutatorBinary.getFactory().Type().createReference(java.lang.Double.class)
	));

	public FloatNoiseOp() {
		super();

		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new FloatReverseMutator(this.mutatorBinary.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		// Vemos se é um opareando.
		if(!(element instanceof CtBinaryOperator)) return false;
		CtBinaryOperator operator = (CtBinaryOperator) element;


		return this.types.stream().anyMatch(type -> {
			return (
				operator.getRightHandOperand().getType().isSubtypeOf(type) ||
				operator.getLeftHandOperand().getType().isSubtypeOf(type) ||
				operator.getType().isSubtypeOf(type)
			);
		});
	}

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		boolean successful = false;
		try {

			CtLiteral ctst = (CtLiteral) operation.getOriginal();
			CtLiteral fix = (CtLiteral) operation.getModified();

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

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
		throws IllegalAccessException {
        CtBlock target = (CtBlock) point.getCodeElement();
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(target);
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement());

		return operation;
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

	/** Return the list of CtElements Mutanted */
	public List<MutantCtElement> getMutants(CtElement element) {
		CtBlock target = (CtBlock) element;
		List<MutantCtElement> mutations = this.mutatorBinary.execute(target);
		return mutations;
    }

	@Override
	public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
		try {
			CtBlock ctst = (CtBlock) opInstance.getOriginal();
			CtBlock fix = (CtBlock) opInstance.getModified();
			fix.replace(ctst);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}

}
