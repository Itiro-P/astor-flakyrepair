package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

@SuppressWarnings({"rawtypes", "unchecked"})
/**
 * Mutator que insere chamadas a `Collections.sort(...)` antes da primeira
 * asserção em um bloco, para ordenar coleções que podem causar testes flaky.
 * @author Pedro Itiro Nagao
 */
public class SortInjectionMutator extends SpoonMutator<CtBlock> {
    private Set<String> collections = new HashSet<>();


    public SortInjectionMutator(Factory factory) {
        super(factory);

        this.collections.add("add");
		this.collections.add("remove");
		this.collections.add("size");
		this.collections.add("iterator");
		this.collections.add("contains");
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        CtBlock block = (CtBlock) toMutate;

        List<CtLocalVariable> collectionVars = block.getElements(new TypeFilter<>(CtLocalVariable.class));

        for (CtLocalVariable var : collectionVars) {

            // Verifica se a variável local parece ser uma coleção (heurística).
            if (!isCollection(var.getType())) continue;


            // Localiza a primeira chamada de `assert*` no bloco; se não houver,
            // não faz sentido inserir a ordenação.
            CtInvocation firstAssert = block.getElements(new TypeFilter<>(CtInvocation.class))
                .stream()
                .filter(inv -> inv.getExecutable().getSimpleName().startsWith("assert"))
                .findFirst()
                .orElse(null);

            if (firstAssert == null) continue;

            // Trabalha em uma cópia do bloco para criar a variante mutada.
            CtBlock mutatedBlock = block.clone();

            CtVariableAccess varAccess = factory.createVariableRead(var.getReference(), false);

            CtTypeReference collectionsType = factory.Type().createReference(java.util.Collections.class);
            CtExecutableReference sortRef = factory.Executable().createReference(
                collectionsType,
                factory.Type().voidPrimitiveType(),
                "sort"
            );
            // Cria a invocação `Collections.sort(var)` que será inserida.
            CtInvocation sortCall = factory.createInvocation(
                factory.createTypeAccess(collectionsType),
                sortRef,
                varAccess
            );

            CtInvocation assertInClone = mutatedBlock.getElements(new TypeFilter<>(CtInvocation.class))
                .stream()
                .filter(inv -> inv.getExecutable().getSimpleName().startsWith("assert"))
                .findFirst()
                .orElse(null);

            // Insere a chamada de sort antes da primeira asserção na cópia.
            if (assertInClone != null) {
                assertInClone.insertBefore(sortCall);
                result.add(new MutantCtElement(mutatedBlock, 1.0));
            }
        }

        return result;
    }

    /**
	 * Método helper para checar se um tipo é uma colećão
	 * @param typeRef o tipo
	 * @return 'true' se é uma colećão.
	 */
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
    public String key() { return "sortedInjectionMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}