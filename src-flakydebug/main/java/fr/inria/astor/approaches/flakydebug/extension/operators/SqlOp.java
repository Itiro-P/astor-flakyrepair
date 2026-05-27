package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLGeneralizationMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLDisorderMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLOrderChangeMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

/**
 * Operador que altera requisições SQL para simular resultados inconsistentes.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class SqlOp extends AutonomousOperator {
    MutatorComposite mutatorBinary = null;

    public SqlOp() {
        super();
        this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
		
        this.mutatorBinary.getMutators().add(new SQLGeneralizationMutator(this.mutatorBinary.getFactory()));
		this.mutatorBinary.getMutators().add(new SQLDisorderMutator(this.mutatorBinary.getFactory()));
		this.mutatorBinary.getMutators().add(new SQLOrderChangeMutator(this.mutatorBinary.getFactory()));
    }

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		
		if (element == null) {
			return false;
		}

		// Caso 1: A query é uma constante ou literal direta
		if (element instanceof CtLiteral) {
			Object value = ((CtLiteral<?>) element).getValue();
			if (value instanceof String) {
				return isQuery((String) value);
			}
		} 
		
		// Caso 2: Declaração de variável local (ex: String sql = "SELECT...")
		else if (element instanceof CtLocalVariable) {
			CtExpression<?> assignment = ((CtLocalVariable<?>) element).getAssignment();
			if (assignment instanceof CtLiteral) {
				Object value = ((CtLiteral<?>) assignment).getValue();
				if (value instanceof String) {
					return isQuery((String) value);
				}
			}
		} 
		
		// Caso 3: Atribuição posterior (ex: sql = "SELECT...")
		else if (element instanceof CtAssignment) {
			CtExpression<?> assignment = ((CtAssignment<?, ?>) element).getAssignment();
			if (assignment instanceof CtLiteral) {
				Object value = ((CtLiteral<?>) assignment).getValue();
				if (value instanceof String) {
					return isQuery((String) value);
				}
			}
		}

		return false;
	}

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

    private boolean isQuery(String str) {
		if (str == null) {
			return false;
		}
		// O .trim() mata espaços ou quebras de linha que jogam o SELECT para frente
		String cleaned = str.trim().toUpperCase();
		
		return cleaned.startsWith("SELECT") && cleaned.contains("FROM");
    }

    public List<MutantCtElement> getMutants(CtElement element) {
        return this.mutatorBinary.execute(element);
    }

    protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
            throws IllegalAccessException {

        CtElement element = point.getCodeElement();

        // O original e o modificado são statements
        OperatorInstance operation = new OperatorInstance();
        operation.setOriginal(element);
        operation.setModified(fix.getElement());
        operation.setOperationApplied(this);
        operation.setModificationPoint(point);
        return operation;
    }

    @Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}
}