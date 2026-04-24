package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakyrepair.extension.operators.mutators.FinalModifierInjectionMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

/**
 * Operator que injeta o modificador `final` em métodos candidatos.
 *
 * Usa um `MutatorComposite` que produz variantes do método com o
 * modificador `final` adicionado. Os métodos criados são usados para gerar
 * instâncias de operador que podem ser aplicadas/undo no modelo.
 *
 * Autor: Pedro I. Nagao
 */
@SuppressWarnings("rawtypes")
public class FinalModifierInjectionOp extends AutonomousOperator {

	MutatorComposite mutatorBinary = null;
	public FinalModifierInjectionOp() {
		super();
		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new FinalModifierInjectionMutator(this.mutatorBinary.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement el = point.getCodeElement();
		return (el.getParent(CtMethod.class) != null);
	}

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		boolean successful = false;
		try {

			// Substitui o método original pelo método mutado (com `final`).
			CtMethod ctst = (CtMethod) operation.getOriginal();
			CtMethod fix = (CtMethod) operation.getModified();

			ctst.replace(fix);
			successful = true;
			operation.setSuccessfulyApplied(successful);

			log.debug(" applied: " + ctst.getParent().toString());

		} catch (Exception ex) {
			log.error("Error applying an operation, exception: " + ex.getMessage());
			operation.setExceptionAtApplied(ex);
			operation.setSuccessfulyApplied(false);
		}
		return successful;
	}

	@Override
	public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {
		List<OperatorInstance> ops = new ArrayList<>();

		CtMethod targetMethod = modificationPoint.getCodeElement().getParent(CtMethod.class);
		
		if (targetMethod == null) return ops;

		List<MutantCtElement> mutations = getMutants(targetMethod);
		for (MutantCtElement mutantCtElement : mutations) {
			try {
				OperatorInstance opInstance = createModificationInstance(modificationPoint, mutantCtElement, targetMethod);
				if (opInstance != null)
					ops.add(opInstance);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return ops;
	}

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix, CtMethod target)
		throws IllegalAccessException {
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(target);
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement()); 

		return operation;
	}

	private List<MutantCtElement> getMutants(CtElement element) {
		CtMethod target = (CtMethod) element;
		List<MutantCtElement> mutations = this.mutatorBinary.execute(target);
		return mutations;
    }

	@Override
	public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
		try {
			CtMethod ctst = (CtMethod) opInstance.getOriginal();
			CtMethod fix = (CtMethod) opInstance.getModified();
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
