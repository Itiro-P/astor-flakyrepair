package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.approaches.flakydebug.extension.operators.InvocationReplacementOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.LiteralMultiplierOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.ShuffleListOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.ShuffleMapOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.ShuffleSetOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FdRepairSpace extends OperatorSpace {
    public FdRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new LiteralMultiplierOp());
        super.register(new ShuffleListOp());
        super.register(new ShuffleMapOp());
        super.register(new ShuffleSetOp());
    }
}
