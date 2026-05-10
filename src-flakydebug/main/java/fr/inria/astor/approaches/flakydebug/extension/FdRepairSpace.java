package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.approaches.flakydebug.extension.operators.EqualComparatorOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.InvocationReplacementOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.LinkedInjectorOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.LiteralMultiplierOp;
import fr.inria.astor.approaches.flakydebug.extension.operators.SortCollectionOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FdRepairSpace extends OperatorSpace {
    public FdRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new LinkedInjectorOp());
        super.register(new SortCollectionOp());
        super.register(new LiteralMultiplierOp());
        super.register(new EqualComparatorOp());
        /** 
         * Esse operador so ajuda em casos restritos.
         * Fora estes, ocorre apenas falsos positivos.
         * Então, por enquanto nao utilizaremos.
         */
        //super.register(new FinalModifierInjectionOp());
    }
}
