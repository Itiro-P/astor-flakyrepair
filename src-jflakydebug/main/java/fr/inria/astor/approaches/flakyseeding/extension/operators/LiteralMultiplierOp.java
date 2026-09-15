package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.LiteralMultiplierMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador que multiplica literais numéricos de certos métodos por um fator (ex: 2x). 
 * Útil para lidar com testes flaky causados por valores limite ou condições de corrida que dependem de tempos ou contagens específicas.
 * (Até agora) não foi constatado um PR que sofre desta instabilidade.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"unchecked"})
public class LiteralMultiplierOp extends Operator {
	private CtTypeReference<?> numberType;
	private CtTypeReference<?> timeUnitType;
	private CtTypeReference<?> durationType;

	public LiteralMultiplierOp() {
		super();

		this.numberType = this.mutatorComposite.factory.createCtTypeReference(Long.class);
		this.timeUnitType = this.mutatorComposite.factory.createCtTypeReference(TimeUnit.class);
		this.durationType = this.mutatorComposite.factory.createCtTypeReference(Duration.class);
        this.mutatorComposite.getMutators().add(new LiteralMultiplierMutator(this.mutatorComposite.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		if (!(element instanceof CtInvocation)) return false;

		CtInvocation<?> invocation = (CtInvocation<?>) element;
		List<CtExpression<?>> arguments = invocation.getArguments();

		boolean hasDurationArg = arguments.stream().anyMatch(arg ->
			arg.getType() != null && arg.getType().isSubtypeOf(this.durationType));

		boolean hasLongArg = arguments.stream().anyMatch(arg -> {
			if (!(arg instanceof CtLiteral || arg instanceof CtVariableRead)) return false;
			CtTypeReference<?> type = arg.getType();
			if (type == null) return false;
			return type.isPrimitive()
				? type.getSimpleName().equals("long")
				: type.isSubtypeOf(this.numberType);
		});

		boolean hasTimeUnitArg = arguments.stream().anyMatch(arg ->
			arg.getType() != null && arg.getType().isSubtypeOf(this.timeUnitType));

		// Duration sozinho já encapsula número+unidade; TimeUnit precisa de um long junto
		return hasDurationArg || (hasLongArg && hasTimeUnitArg);
	}
}