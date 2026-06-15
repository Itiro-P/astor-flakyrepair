package fr.inria.astor.approaches.flakydebug.extension.operators;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLGeneralizationMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLDisorderMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.SQLOrderChangeMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

/**
 * Operador que altera requisições SQL para simular resultados inconsistentes.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("unchecked")
public class SqlOp extends Operator {

    public SqlOp() {
        super();
		
        this.mutatorComposite.getMutators().add(new SQLGeneralizationMutator(this.mutatorComposite.getFactory()));
		this.mutatorComposite.getMutators().add(new SQLDisorderMutator(this.mutatorComposite.getFactory()));
		this.mutatorComposite.getMutators().add(new SQLOrderChangeMutator(this.mutatorComposite.getFactory()));
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

    private boolean isQuery(String str) {
		if (str == null) {
			return false;
		}
		// O .trim() mata espaços ou quebras de linha que jogam o SELECT para frente
		String cleaned = str.trim().toUpperCase();
		
		return cleaned.startsWith("SELECT") && cleaned.contains("FROM");
    }
}