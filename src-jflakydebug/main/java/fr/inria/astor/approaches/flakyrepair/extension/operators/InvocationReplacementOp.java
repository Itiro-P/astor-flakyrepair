package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakyrepair.extension.operators.mutators.MethodConstraintRelaxationMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;

/**
 * Operator que substitui invocações por variantes menos restritivas (ex.:
 * métodos que não exigem ordem). Encapsula o `MethodConstraintRelaxationMutator`.
 * @author Pedro Itiro Nagao
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class InvocationReplacementOp extends AutonomousOperator {

	MutatorComposite mutatorBinary = null;
	public InvocationReplacementOp() {
		super();
		// Registra os mutators no Factory para uso do programa
		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new MethodConstraintRelaxationMutator(this.mutatorBinary.getFactory()));
	}

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
		// Aplica apenas em pontos que sejam invocações (chamadas de método).
		return (point.getCodeElement() instanceof CtInvocation);
    }

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		boolean successful = false;
		try {

			// Substitui a invocação original pela versão renomeada/relaxada.
			CtInvocation ctst = (CtInvocation) operation.getOriginal();
			CtInvocation fix = (CtInvocation) operation.getModified();

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
        CtInvocation target = (CtInvocation) point.getCodeElement();
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
		CtInvocation target = (CtInvocation) element;
		List<MutantCtElement> mutations = this.mutatorBinary.execute(target);
		return mutations;
    }

	@Override
	public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
		try {
			CtInvocation ctst = (CtInvocation) opInstance.getOriginal();
			CtInvocation fix = (CtInvocation) opInstance.getModified();
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
