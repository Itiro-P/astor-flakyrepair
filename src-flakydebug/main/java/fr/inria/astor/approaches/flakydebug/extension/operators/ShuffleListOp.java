package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.ShuffleListMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador que insere o método `.shuffle()` de coleções antes do primeiro método
 * de asserção em um bloco.
 *
 * Breve: detecta blocos com variáveis-coleção e asserções; usa
 * `ShuffleCollectionMutator` para gerar variantes onde a coleção é desordenada antes
 * da asserção, forćando flakiness por ordem não determinística.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class ShuffleListOp extends AutonomousOperator {
	MutatorComposite mutatorBinary = null;
	public ShuffleListOp() {
		super();

		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new ShuffleListMutator(this.mutatorBinary.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		
		// 1. Verifica se é uma invocação (Ex: assertEquals)
		if (!(element instanceof CtInvocation)) return false;

		CtInvocation invocation = (CtInvocation) element;
		String methodName = invocation.getExecutable().getSimpleName().toLowerCase();

		// 2. Filtra apenas métodos de asserção
		if (!methodName.startsWith("assert")) return false;

		// 3. Checa se algum dos argumentos passados é uma variável do tipo coleção
		List<CtExpression> args = invocation.getArguments();
		return args.stream()
				.filter(arg -> arg instanceof CtVariableRead)
				.anyMatch(arg -> isList(arg.getType()));
	}

	/**
	 * Método helper para checar se um tipo é uma lista
	 * @param typeRef o tipo
	 * @return 'true' se é uma colećão.
	 */
    private boolean isList(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(typeRef.getFactory().Type().createReference(java.util.List.class));
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
		
		CtElement element = point.getCodeElement();
		CtBlock target = element instanceof CtBlock ? (CtBlock) element : element.getParent(CtBlock.class);

		if (target == null) {
			log.error("Não foi possível encontrar um CtBlock pai para o elemento: " + element.getClass());
			return null;
		}

		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(target); // O "Original" aqui será o bloco todo
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement()); // O "fix" já é o bloco mutado vindo do mutator

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
		// Passamos o elemento (o assert) para o mutador
		return this.mutatorBinary.execute(element);
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

