package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.approaches.flakydebug.extension.operators.*;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FdRepairSpace extends OperatorSpace {
    public FdRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new LiteralMultiplierOp());
        super.register(new ShuffleCollectionOp());
        super.register(new FloatReverseOp());
        super.register(new FloatNoiseOp());
        super.register(new SqlOp());
    }
}
