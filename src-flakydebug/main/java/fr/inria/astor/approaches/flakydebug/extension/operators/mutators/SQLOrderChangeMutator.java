package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que inverte a direção de ORDER BY em queries SQL (ASC↔DESC),
 * expondo falhas relacionadas à dependência da ordem de resultados.
 * Exemplo de (possível) PR afetado: https://github.com/apache/iotdb/pull/4459
 * @author Pedro Itiro Nagao
 */
public class SQLOrderChangeMutator extends Mutator<CtElement> {

    private static final Pattern CHECK_PATTERN = Pattern.compile(
        "(SELECT)\\s(.+)\\s(FROM)\\s(.+)\\s(ORDER\\s+BY)\\s(.+)\\s(ASC|DESC)\\s*$",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public SQLOrderChangeMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        // Caso 1: mod_point é o próprio literal — retorna literal clonado (tipo compatível)
        if (toMutate instanceof CtLiteral) {
            @SuppressWarnings("unchecked")
            CtLiteral<String> literal = (CtLiteral<String>) toMutate;
            MutantCtElement mutant = mutateLiteralInPlace(literal);
            if (mutant != null) result.add(mutant);

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

    /**
     * Usado apenas no Caso 1, onde o mod_point já é o literal e pode ser
     * retornado diretamente como mutante (tipos compatíveis com o replace).
     */
    private MutantCtElement mutateLiteralInPlace(CtLiteral<String> original) {
        String mutatedValue = computeMutation(original.getValue());
        if (mutatedValue == null) return null;
        CtLiteral<String> clone = (CtLiteral<String>) original.clone();
        clone.setValue(mutatedValue);
        return new MutantCtElement(clone, 1.0);
    }

    /**
     * Aplica a mutação na string da query: inverte ASC↔DESC.
     * Usa Matcher.group() para capturar o valor real do grupo — não "$7" literal.
     *
     * @return a query mutada, ou null se o padrão não casar ou a mutação for idêntica.
     */
    private String computeMutation(String query) {
        if (query == null) return null;
        Matcher m = CHECK_PATTERN.matcher(query);
        if (!m.find()) return null;

        // grupo 7 contém o valor real de ASC ou DESC
        String currentOrder = m.group(7);
        String newOrder = reverseOrder(currentOrder);

        // Reconstrói a query preservando os grupos 1–6 e substituindo apenas o 7
        String mutated = m.replaceFirst("$1 $2 $3 $4 $5 $6 " + newOrder).trim();
        return mutated.equals(query) ? null : mutated;
    }

    /**
     * Inverte ASC para DESC e vice-versa.
     */
    private String reverseOrder(String order) {
        if (order.equalsIgnoreCase("ASC")) return "DESC";
        if (order.equalsIgnoreCase("DESC")) return "ASC";
        return order;
    }
}