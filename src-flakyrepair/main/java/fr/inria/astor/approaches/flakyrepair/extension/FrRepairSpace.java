package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakyrepair.extension.operators.InvocationReplacementOp;
import fr.inria.astor.approaches.flakyrepair.extension.operators.LinkedInjectorOp;
import fr.inria.astor.approaches.flakyrepair.extension.operators.SortedCollectionOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class FrRepairSpace extends OperatorSpace {
    public FrRepairSpace() {
        super.register(new InvocationReplacementOp());
        super.register(new LinkedInjectorOp());
        super.register(new SortedCollectionOp());
        /** 
         * Esse operador so ajuda em casos restritos.
         * Fora estes, ocorre apenas falsos positivos.
         * Então, por enquanto nao utilizaremos.
         */
        //super.register(new FinalModifierInjectionOp());
    }
}
