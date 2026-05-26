package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledJSON;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Mutator que injeta flakiness em JSON objects embaralhando seus entries antes do primeiro
 * statement que passa a variável para outro método (ponto de consumo).
 *
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ShuffleJSONMutator extends SpoonMutator<CtLocalVariable> {
    private CtTypeReference replacementRef = this.getFactory().Type().createReference(ShuffledJSON.class);
    private CtTypeReference jsonRef = this.getFactory().Type().createReference(org.json.simple.JSONObject.class);

    public ShuffleJSONMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            if (ctc.getType() == null || !ctc.getType().isSubtypeOf(jsonRef)) {
                return result;
            }

            CtConstructorCall mutantCtc = ctc.clone();
            mutantCtc.setType(replacementRef);

            MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
            result.add(mutant);
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;

            // check variable type or assignment type
            CtTypeReference<?> varType = ctl.getType();
            CtExpression assign = ctl.getAssignment();
            CtTypeReference<?> assignType = assign != null ? assign.getType() : null;
            if ((varType == null || !varType.isSubtypeOf(jsonRef)) && (assignType == null || !assignType.isSubtypeOf(jsonRef))) {
                return result;
            }

            CtLocalVariable mutantVar = ctl.clone();
            if (mutantVar.getAssignment() instanceof CtConstructorCall) {
                // new JSONObject() → new ShuffledJSON()
                ((CtConstructorCall) mutantVar.getAssignment()).setType(replacementRef);
            } else if (mutantVar.getAssignment() instanceof CtInvocation) {
                CtExpression original = mutantVar.getAssignment().clone();
                CtConstructorCall wrapped = factory.createConstructorCall(replacementRef, original);
                mutantVar.setAssignment(wrapped);
            }
            result.add(new MutantCtElement(mutantVar, 1));
        }

        return result;
    }

    @Override
    public String key() { return "shuffleJSONMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}