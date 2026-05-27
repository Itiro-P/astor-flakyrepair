package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que altera requisições SQL substituindo colunas explícitas por SELECT *,
 * potencialmente expondo falhas relacionadas à dependência da ordem ou número de colunas
 * retornadas.
 *
 * @author Pedro Itiro Nagao
 */
public class SQLOrderChangeMutator extends SpoonMutator<CtElement> {
    private static final String CHECK_REGEX = "(?i)(SELECT)\\s(.+)\\s(FROM)\\s(.+)\\s(ORDER\\sBY)\\s(.+)\\s(ASC|DESC)";

    public SQLOrderChangeMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (!(toMutate instanceof CtLiteral)) {
            return result;
        }

        CtLiteral<?> literal = (CtLiteral<?>) toMutate;

        if (!(literal.getValue() instanceof String)) {
            return result;
        }

        String original = (String) literal.getValue();


        // 2. Substitui a ordenação por outra ao contrário
        // $1 = SELECT
        // $2 = Colunas
        // $3 = FROM
        // $4 = Tabela e condições (WHERE)
        // $5 = ORDER BY
        // $6 = reverOrder(ASC / DESC)
        String mutated = original.replaceFirst(CHECK_REGEX, "$1 $2 $3 $4 $5 " + reverseOrder("$6"));

        if (mutated.equals(original)) {
            return result;
        }

        @SuppressWarnings("unchecked")
        CtLiteral<String> clone = (CtLiteral<String>) literal.clone();
        clone.setValue(mutated);

        result.add(new MutantCtElement(clone, 1.0));
        return result;
    }

    /**
     * Inverte a ordenação de ASC para DESC e vice-versa.
     * @param order A ordem usada.
     * @return A ordem invertida ou a mesma se não for ASC ou DESC.
     */
    private String reverseOrder(String order) {
        if (order.equalsIgnoreCase("ASC")) {
            return "DESC";
        } else if (order.equalsIgnoreCase("DESC")) {
            return "ASC";
        }
        return order; // Retorna o mesmo se não for ASC ou DESC
    }

    @Override
    public String key() {
        return "sqlOrderChangeMutator";
    }

    @Override
    public void setup() {
    }

    @Override
    public int levelMutation() {
        return 1;
    }
}