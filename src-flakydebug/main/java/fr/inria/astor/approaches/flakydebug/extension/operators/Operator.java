package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.declaration.CtElement;

/**
 * @brief Classe base para implementação de operadores.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Operator extends AutonomousOperator {
    protected MutatorComposite mutatorComposite = new MutatorComposite(MutationSupporter.getFactory());

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        boolean successful = false;
        try {
            CtElement ctst = operation.getOriginal();
            CtElement fix = operation.getModified();

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

    @Override
    public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
        try {
            CtElement ctst = opInstance.getOriginal();
            CtElement fix = opInstance.getModified();
            fix.replace(ctst);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<MutantCtElement> getMutants(CtElement element) {
        return this.mutatorComposite.execute(element);
    }

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
		throws IllegalAccessException {
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(point.getCodeElement());
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement());

		return operation;
    }

    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        return false;
    }
}
