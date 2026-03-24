package fr.inria.astor.approaches.flakyrepair.extension.operators;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class GlobalVariableCopyOp extends AutonomousOperator {

    public GlobalVariableCopyOp() {
        super();
    }
    
    @Override
    public boolean canBeAppliedToPoint(ModificationPoint mp) {
        CtElement element = mp.getCodeElement();
        CtType<?> declaringClass = element.getParent(CtType.class);
        
        if (declaringClass == null) return false;
        
        // Caso 1: CtFieldAccess (campos de instância - this.field) na mesma classe
        if (element instanceof CtFieldAccess) {
            CtFieldAccess<?> fieldAccess = (CtFieldAccess<?>) element;
            CtField<?> field = fieldAccess.getVariable().getFieldDeclaration();
            if (field == null) return false;
            return declaringClass == field.getParent(CtType.class);
        }
        
        // Caso 2: CtVariableAccess para variáveis estáticas não-constantes na mesma classe
        if (element instanceof CtVariableAccess) {
            try {
                CtVariableAccess<?> varAccess = (CtVariableAccess<?>) element;
                if (!(varAccess.getVariable() instanceof CtFieldReference)) {
                    return false;
                }
                
                CtFieldReference<?> fieldRef = (CtFieldReference<?>) varAccess.getVariable();
                CtField<?> field = fieldRef.getFieldDeclaration();
                if (field == null) return false;
                
                boolean isStatic = field.hasModifier(spoon.reflect.declaration.ModifierKind.STATIC);
                boolean isFinal = field.hasModifier(spoon.reflect.declaration.ModifierKind.FINAL);
                
                return isStatic && !isFinal && declaringClass == field.getParent(CtType.class);
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        try {
            CtFieldAccess fieldAccess = (CtFieldAccess<?>) operation.getOriginal();
            CtMethod method = fieldAccess.getParent(CtMethod.class);

            if(method == null) return false;

            CtBlock<?> body = method.getBody();

            if(body == null) return false;

            CtTypeReference fieldType = fieldAccess.getType();
            CtLocalVariable localVar = fieldAccess.getFactory().Core().createLocalVariable();
            localVar.setType(fieldType);
            localVar.setDefaultExpression(fieldAccess.clone());

            body.addStatement(0, localVar);

            fieldAccess.replace(localVar);

            // marca modificado no opInstance, para facilitar no undoChangesInModel
            operation.setModified(localVar);

        } catch(Exception ex) {
            log.error("Error applying an operation, exception: " + ex.getMessage());
            return false;
        }
        return false;
    }

    @Override
    public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
        try {
            CtElement modified = opInstance.getModified();
            CtFieldAccess<?> original = (CtFieldAccess<?>) opInstance.getOriginal();

            if (modified != null) {
                modified.replace(original.clone());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        return true;
    }
}
