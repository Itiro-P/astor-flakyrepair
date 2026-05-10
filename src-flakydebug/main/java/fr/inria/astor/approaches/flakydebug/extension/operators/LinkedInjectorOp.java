package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.LinkedInjectorMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;

/**
 * Operador que troca uma coleção por sua versão "encadeada" para preservar a ordem dos elementos.
 *
 * Encapsula o `LinkedInjectorMutator` e cria instâncias de operador
 * a partir das mutações encontradas (construtores e variáveis locais).
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class LinkedInjectorOp extends AutonomousOperator {

	MutatorComposite mutatorBinary = null;
	public LinkedInjectorOp() {
		super();
		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new LinkedInjectorMutator(this.mutatorBinary.getFactory()));
	}

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement el = point.getCodeElement();
		// Pode aplicar quando o ponto de modificação é a criação de uma coleção
		// (`new HashMap()`) ou uma variável local cujo RHS é uma construção.
		return (el instanceof CtConstructorCall) || (el instanceof CtLocalVariable);
    }

	@Override
	public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
		boolean successful = false;
		try {

			// Substitui o elemento AST (construtor/variável) pela versão mutada.
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

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
		throws IllegalAccessException {
        CtElement target = point.getCodeElement();
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
		List<MutantCtElement> mutations = this.mutatorBinary.execute(element);
		return mutations;
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

	@Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}

}
