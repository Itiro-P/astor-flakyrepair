package fr.inria.astor.approaches.flakyrepair.extension;
import fr.inria.astor.approaches.flakyrepair.extension.operators.FinalModifierInjectionOp;
import fr.inria.astor.approaches.flakyrepair.extension.operators.InvocationReplacementOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FrRepairSpace extends OperatorSpace {
    public FrRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new FinalModifierInjectionOp());
    }
}
