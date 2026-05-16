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
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador que troca uma coleção por sua versão "encadeada" para preservar a ordem dos elementos.
 *
 * Encapsula o `LinkedInjectorMutator` e cria instâncias de operador
 * a partir das mutações encontradas (construtores e variáveis locais).
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class LinkedInjectorOp extends AutonomousOperator {
	private static Factory factory;
	MutatorComposite mutatorBinary = null;
	public LinkedInjectorOp() {
		super();
		LinkedInjectorOp.factory = this.mutatorBinary.getFactory();
		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new LinkedInjectorMutator(this.mutatorBinary.getFactory()));
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
					.anyMatch(arg -> isCollection(arg.getType()));
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

	/**
	 * Método helper para checar se um tipo é uma colećão
	 * @param typeRef o tipo
	 * @return 'true' se é uma colećão.
	 */
	private boolean isCollection(CtTypeReference<?> typeRef) {
		if (typeRef == null) return false;
		
		try {
			// Criamos as referências para as interfaces base do Java
			CtTypeReference<java.util.Set> colRef = factory.Type().createReference(java.util.Set.class);
			CtTypeReference<java.util.Map> mapRef = factory.Type().createReference(java.util.Map.class);

			if (typeRef.isSubtypeOf(colRef) || typeRef.isSubtypeOf(mapRef)) {
				return true;
			}
		} catch (Exception e) {
			// Silencioso: se falhar o check estrutural, usamos o fallback
		}

		String name = typeRef.getQualifiedName();
		return name.contains("Map") || name.contains("Set");
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
