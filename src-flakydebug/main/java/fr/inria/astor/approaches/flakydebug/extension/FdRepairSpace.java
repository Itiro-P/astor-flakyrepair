package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.approaches.flakydebug.extension.operators.InvocationReplacementOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.LiteralMultiplierOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.ShuffleCollectionOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FdRepairSpace extends OperatorSpace {
    public FdRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new LiteralMultiplierOp());
        super.register(new ShuffleCollectionOp());
    }
}
