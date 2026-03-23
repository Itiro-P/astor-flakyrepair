package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakyrepair.extension.operators.LiteralMultiplierOp;
import fr.inria.astor.approaches.jmutrepair.operators.ReturnExpresionMutOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FrOperatorSpace extends OperatorSpace {
    public FrOperatorSpace() {
        super.register(new LiteralMultiplierOp());
        super.register(new ReturnExpresionMutOp());
    }
}
