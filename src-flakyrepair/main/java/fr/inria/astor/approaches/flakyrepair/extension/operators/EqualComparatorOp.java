package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import fr.inria.astor.approaches.flakyrepair.extension.operators.mutators.EqualComparatorMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

@SuppressWarnings("rawtypes")
public class EqualComparatorOp extends AutonomousOperator {
    private static HashSet<String> allowedMethods = new HashSet<>(Arrays.asList("toString", "toArray"));

    MutatorComposite mutatorBinary = null;

    public EqualComparatorOp() {
        super();
        this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new EqualComparatorMutator(this.mutatorBinary.getFactory()));
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement el = point.getCodeElement();

        if (!(el instanceof CtInvocation)) return false;
        CtInvocation<?> inv = (CtInvocation<?>) el;

        String methodName = inv.getExecutable().getSimpleName();
        if (!methodName.equals("assertEquals")) return false;

        List<CtExpression<?>> args = inv.getArguments();
        if (args.size() < 2) return false;

        return isConverterCall(args.get(0)) && isConverterCall(args.get(1));
    }

    private boolean isConverterCall(CtExpression<?> expr) {
        if (!(expr instanceof CtInvocation<?>)) return false;
        CtInvocation<?> call = (CtInvocation<?>) expr;
        return allowedMethods.contains(call.getExecutable().getSimpleName());
    }

	@Override
	public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {
		List<OperatorInstance> ops = new ArrayList<>();

		CtMethod targetMethod = modificationPoint.getCodeElement().getParent(CtMethod.class);
		if (targetMethod == null) return ops;

		List<MutantCtElement> mutations = getMutants(modificationPoint.getCodeElement());

		for (MutantCtElement mutantCtElement : mutations) {
			try {
				OperatorInstance opInstance = createModificationInstance(
					modificationPoint, mutantCtElement, targetMethod
				);
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
		operation.setOriginal(target);           // CtMethod original
		operation.setModified(fix.getElement()); // CtMethod mutado
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		return operation;
	}

	private List<MutantCtElement> getMutants(CtElement element) {
		CtMethod targetMethod = element.getParent(CtMethod.class);
		return this.mutatorBinary.execute(targetMethod);
	}

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		try {
			CtMethod original = (CtMethod) operation.getOriginal();
			CtMethod modified = (CtMethod) operation.getModified();
			original.replace(modified);
			operation.setSuccessfulyApplied(true);
			return true;
		} catch (Exception ex) {
			log.error("Error applying: " + ex.getMessage());
			operation.setExceptionAtApplied(ex);
			operation.setSuccessfulyApplied(false);
			return false;
		}
	}

	@Override
	public boolean undoChangesInModel(OperatorInstance operation, ProgramVariant p) {
		try {
			CtMethod original = (CtMethod) operation.getOriginal();
			CtMethod modified = (CtMethod) operation.getModified();
			modified.replace(original);
			return true;
		} catch (Exception ex) {
			log.error("Error undoing: " + ex.getMessage());
			return false;
		}
	}

    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        return false;
    }
}