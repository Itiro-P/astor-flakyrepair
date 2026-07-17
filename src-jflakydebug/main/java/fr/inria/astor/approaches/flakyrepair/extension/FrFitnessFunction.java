package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdFitnessFunction;

public class FrFitnessFunction extends FdFitnessFunction {
	public double getWorstMaxFitnessValue() {
        return 1.0;
    }
}
