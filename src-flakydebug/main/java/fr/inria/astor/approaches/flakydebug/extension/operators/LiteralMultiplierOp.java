package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.LiteralMultiplierMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

/**
 * Operator que multiplica literais numéricos de certos métodos por um fator (ex: 2x). 
 * Útil para lidar com testes flaky causados por valores limite ou condições de corrida que dependem de tempos ou contagens específicas.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class LiteralMultiplierOp extends AutonomousOperator {
	private Set<String> allowedMethods = new HashSet<>(Arrays.asList("sleep", "wait", "join", "countDown", "incrementAndGet", "decrementAndGet"));

	MutatorComposite mutatorBinary = null;
	public LiteralMultiplierOp() {
		super();

		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new LiteralMultiplierMutator(this.mutatorBinary.getFactory()));
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

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		boolean successful = false;
		try {

			CtBlock ctst = (CtBlock) operation.getOriginal();
			CtBlock fix = (CtBlock) operation.getModified();

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