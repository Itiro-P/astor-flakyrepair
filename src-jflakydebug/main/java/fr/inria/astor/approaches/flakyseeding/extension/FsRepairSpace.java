package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakyseeding.extension.operators.LiteralMultiplierOp;
import fr.inria.astor.approaches.flakyseeding.extension.operators.FloatNoiseOp;
import fr.inria.astor.approaches.flakyseeding.extension.operators.FloatReverseOp;
import fr.inria.astor.approaches.flakyseeding.extension.operators.ShuffleCollectionOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FsRepairSpace extends OperatorSpace {
    public FsRepairSpace() {
        super.register(new FloatNoiseOp());
        super.register(new FloatReverseOp());
        //super.register(new InvocationReplacementOp());
        super.register(new LiteralMultiplierOp());
        super.register(new ShuffleCollectionOp());
        //super.register(new SqlOp());
    }
}