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
public class SQLGeneralizationMutator extends SpoonMutator<CtElement> {

    private static final String CHECK_REGEX =
    "(?i)(SELECT)\\s(.+)\\s(FROM)\\s(.+)\\s(ORDER\\sBY|GROUP\\sBY|HAVING)\\s(.+)";

    public SQLGeneralizationMutator(Factory factory) {
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

        // 1. Verifica se a string bate com o padrão exato da sua regex
        if (!original.matches(CHECK_REGEX)) {
            return result;
        }

        // 2. Substitui as colunas (Grupo 2) por '*' e remonta a query com os outros grupos:
        // $1 = SELECT
        // $3 = FROM
        // $4 = Tabela e condições (WHERE)
        // $5 = ORDER BY / GROUP BY / HAVING
        // $6 = Restante da query
        String mutated = original.replaceFirst(CHECK_REGEX, "$1 * $3 $4 $5 $6");

        if (mutated.equals(original)) {
            return result;
        }

        @SuppressWarnings("unchecked")
        CtLiteral<String> clone = (CtLiteral<String>) literal.clone();
        clone.setValue(mutated);

        result.add(new MutantCtElement(clone, 1.0));
        return result;
    }

    @Override
    public String key() {
        return "sqlGeneralizationMutator";
    }

    @Override
    public void setup() {
    }

    @Override
    public int levelMutation() {
        return 1;
    }
}