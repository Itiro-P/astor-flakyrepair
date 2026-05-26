package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que altera requisições SQL removendo cláusulas ORDER BY, GROUP BY e HAVING,
 * o que pode causar alterações na ordem dos resultados retornados, potencialmente
 * expondo falhas relacionadas à ordem dos dados.
 *
 * @author Pedro Itiro Nagao
 */
public class SQLDisorderMutator extends SpoonMutator<CtElement> {

    // O (?si) ativa o case-insensitive (i) e permite que o .* leia quebras de linha (s)
    private static final String SQL_CLAUSE_REGEX = "(?si)\\s*(ORDER\\s+BY|GROUP\\s+BY|HAVING)\\b.*";

    public SQLDisorderMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (!(toMutate instanceof CtLiteral)) {
            return result;
        }

        CtLiteral<?> literal = (CtLiteral<?>) toMutate;

        // Só age em literais string
        if (!(literal.getValue() instanceof String)) {
            return result;
        }

        String original = (String) literal.getValue();

        // Verifica se a string contém alguma das cláusulas alvo
        if (!original.toUpperCase().matches("(?s).*\\b(ORDER\\s+BY|GROUP\\s+BY|HAVING)\\b.*")) {
            return result;
        }

        String cleaned = original.replaceFirst(SQL_CLAUSE_REGEX, "");

        if (cleaned.equals(original)) {
            return result; // Nenhuma cláusula encontrada, não gera mutante
        }

        // Clona o nó original e aplica a mutação na cópia
        @SuppressWarnings("unchecked")
        CtLiteral<String> clone = (CtLiteral<String>) literal.clone();
        clone.setValue(cleaned);

        result.add(new MutantCtElement(clone, 1.0));
        return result;
    }

    @Override
    public String key() {
        return "sqlDisorderMutator";
    }

    @Override
    public void setup() {
    }

    @Override
    public int levelMutation() {
        return 1;
    }
}