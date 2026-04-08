package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.ExpresionMutOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtElement;

public class LinkedInjectorOp extends ExpresionMutOp {
    private static final Map<String, String> classesToSwitch = new HashMap<>();

	static {
		classesToSwitch.put("java.util.ArrayList", "java.util.LinkedList");
		classesToSwitch.put("java.util.HashMap", "java.util.Hashtable");
	}

	public LinkedInjectorOp() {
		super();
	}

    private static String getReplacementClass(String originalClass) {
        return classesToSwitch.get(originalClass);
    }

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement el = point.getCodeElement();
		if (el instanceof CtInvocation<?>) {
			CtInvocation<?> invocation = (CtInvocation<?>) el;
			String targetClass = invocation.getExecutable().getDeclaringType().getQualifiedName();
			return classesToSwitch.containsKey(targetClass);
		}
		return false;
	}

	protected OperatorInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
			throws IllegalAccessException {
        CtInvocation<?> targetInvocation = (CtInvocation<?>) point.getCodeElement();
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(targetInvocation);
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement());

		return operation;
	}

	/** Return the list of CtElements Mutanted */
	@Override
	public List<MutantCtElement> getMutants(CtElement element) {
		List<MutantCtElement> mutations = new ArrayList<>();
		
		if (element instanceof CtNewClass<?>) {
			CtNewClass<?> newClassElement = (CtNewClass<?>) element;
			String oldClass = newClassElement.getExecutable().getDeclaringType().getQualifiedName();
			String newClassName = getReplacementClass(oldClass);
			
			if (newClassName != null) {
				CtNewClass<?> mutant = newClassElement.clone();
				mutant.getExecutable().setDeclaringType(
					mutant.getFactory().Type().createReference(newClassName)
				);
				mutations.add(new MutantCtElement(mutant, 1.0));
			}
		}

		else if (element instanceof CtInvocation<?>) {
			CtInvocation<?> invocation = (CtInvocation<?>) element;
			String oldClass = invocation.getExecutable().getDeclaringType().getQualifiedName();
			String newClassName = getReplacementClass(oldClass);
			
			if (newClassName != null) {
				CtInvocation<?> mutant = invocation.clone();
				mutant.getExecutable().setDeclaringType(
					mutant.getFactory().Type().createReference(newClassName)
				);
				mutations.add(new MutantCtElement(mutant, 1.0));
			}
		}
		
		return mutations;
	}

	@Override
	public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}
    
}
