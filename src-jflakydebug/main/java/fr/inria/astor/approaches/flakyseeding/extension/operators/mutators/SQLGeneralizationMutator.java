package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que altera requisições SQL substituindo colunas explícitas por SELECT *,
 * potencialmente expondo falhas relacionadas à dependência da ordem ou número de colunas
 * retornadas.
 * Exemplo de (possível) PR afetado: https://github.com/apache/iotdb/pull/4459
 * @author Pedro Itiro Nagao
 */
public class SQLGeneralizationMutator extends Mutator<CtElement> {

    private static final String CHECK_REGEX = "(?i)(SELECT)\\s(.+?)\\s(FROM)\\s(.*?)(\\s(?:ORDER\\s+BY|GROUP\\s+BY|HAVING)\\s.+)?$";

    public SQLGeneralizationMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        // Caso 1: mod_point é o próprio literal — retorna literal clonado (tipo compatível)
        if (toMutate instanceof CtLiteral) {
            @SuppressWarnings("unchecked")
            CtLiteral<String> literal = (CtLiteral<String>) toMutate;
            String mutated = computeMutation(literal.getValue());
            if (mutated != null) {
                CtLiteral<String> clone = (CtLiteral<String>) literal.clone();
                clone.setValue(mutated);
                result.add(new MutantCtElement(clone, 1.0));
            }
        // Caso 2: mod_point é CtLocalVariable — deve-se retornar CtLocalVariable clonado
        } else if (toMutate instanceof CtLocalVariable) {
            CtExpression<?> rhs = ((CtLocalVariable<?>) toMutate).getAssignment();
            if (rhs instanceof CtLiteral) {
                @SuppressWarnings("unchecked")
                CtLiteral<String> literal = (CtLiteral<String>) rhs;
                String mutatedValue = computeMutation(literal.getValue());
                if (mutatedValue != null) {
                    @SuppressWarnings("unchecked")
                    CtLocalVariable<String> cloneVar = (CtLocalVariable<String>) toMutate.clone();
                    CtLiteral<String> cloneLit = (CtLiteral<String>) cloneVar.getAssignment();
                    cloneLit.setValue(mutatedValue);
                    result.add(new MutantCtElement(cloneVar, 1.0));
                }
            }

        // Caso 3: mod_point é CtAssignment — deve-se retornar CtAssignment clonado
        } else if (toMutate instanceof CtAssignment) {
            CtExpression<?> rhs = ((CtAssignment<?, ?>) toMutate).getAssignment();
            if (rhs instanceof CtLiteral) {
                @SuppressWarnings("unchecked")
                CtLiteral<String> literal = (CtLiteral<String>) rhs;
                String mutatedValue = computeMutation(literal.getValue());
                if (mutatedValue != null) {
                    @SuppressWarnings("unchecked")
                    CtAssignment<String, String> cloneAssign = (CtAssignment<String, String>) toMutate.clone();
                    CtLiteral<String> cloneLit = (CtLiteral<String>) cloneAssign.getAssignment();
                    cloneLit.setValue(mutatedValue);
                    result.add(new MutantCtElement(cloneAssign, 1.0));
                }
            }
        }
        return result;
    }

    private String computeMutation(String query) {
        if (query == null) return null;
        if(!query.matches(CHECK_REGEX)) return null;

        List<String> groups = new ArrayList<>(Arrays.asList(query.trim().split(" ")));

        // Substitui o grupo 2 (colunas) por "*"
        if(groups.get(1).equals("*")) return null; // Já é generalizado, não gera mutante
        groups.set(1, "*");
        return String.join(" ", groups);
    }
}