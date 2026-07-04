package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.factory.Factory;

/**
 * @brief Classe base para mutators
 * @author Pedro Itiro Nagao
 */
public abstract class Mutator<T> extends SpoonMutator<T> {
    public Mutator(Factory factory) {
        super(factory);
    }

    @Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}

    @Override
	public String key() {
		return this.getClass().getSimpleName();
	}
}
