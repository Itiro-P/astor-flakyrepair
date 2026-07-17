package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdFitnessFunction;

public class FsFitnessFunction extends FdFitnessFunction {

    @Override
	public double getWorstMaxFitnessValue() {
        return 0.0;
    }
}
