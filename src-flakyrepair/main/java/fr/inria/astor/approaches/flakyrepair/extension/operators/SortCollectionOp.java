package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.flakyrepair.extension.operators.mutators.SortInjectionMutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.MutatorComposite;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Operador que insere o método `.sort()` de coleções antes do primeiro método
 * de asserção em um bloco.
 *
 * Breve: detecta blocos com variáveis-coleção e asserções; usa
 * `SortedInjectionMutator` para gerar variantes onde a coleção é ordenada antes
 * da asserção, corrigindo flakiness por ordem não determinística.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class SortCollectionOp extends AutonomousOperator {
	private Set<String> collections = new HashSet<>();

	MutatorComposite mutatorBinary = null;
	public SortCollectionOp() {
		super();

		/**
		 * Métodos que o a interface Collections tem.
		 * Checamos se as classes alvo contém eles uma vez que é
		 * mais fácil assim do que usar checagens internas que foram depreciadas.
		 */
		this.collections.add("add");
		this.collections.add("remove");
		this.collections.add("size");
		this.collections.add("iterator");
		this.collections.add("contains");

		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
        this.mutatorBinary.getMutators().add(new SortInjectionMutator(this.mutatorBinary.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();

		if (!(element instanceof CtBlock)) return false;

		CtBlock block = (CtBlock) element;

		// Verifica se há pelo menos uma variável local que pareça ser coleção.
		boolean hasCollectionVar = block.getElements(new TypeFilter<>(CtLocalVariable.class))
			.stream()
			.anyMatch(var -> isCollection(((CtLocalVariable) var).getType()));

		boolean hasAssert = block.getElements(new TypeFilter<>(CtInvocation.class))
			.stream()
			.anyMatch(inv -> inv.getExecutable().getSimpleName().startsWith("assert"));

		return hasCollectionVar && hasAssert;
	}

	private boolean isCollection(CtTypeReference<?> typeRef) {
		if (typeRef == null) return false;
		
		try {
			CtType<?> typeDecl = typeRef.getTypeDeclaration();
			if (typeDecl == null) return false;

			return typeDecl.getAllMethods().stream()
				.map(m -> m.getSimpleName())
				.anyMatch(this.collections::contains);

		} catch (Exception e) {
			// fallback por nome
			String name = typeRef.getQualifiedName();
			return name.contains("List") || name.contains("Collection") || name.contains("Set");
		}
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

