package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.time.Duration;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.LiteralMultiplierMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador que multiplica literais numéricos de métodos de tempo/espera por um fator (ex: 2x).
 * Útil para detectar testes flaky causados por condições de corrida ou timeouts ajustados.
 * 
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"unchecked"})
public class LiteralMultiplierOp extends Operator {
    private static final Set<String> TIMING_METHOD_NAMES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("sleep", "wait", "await", "delay", "join", "park", "trylock"))
    );

    private final CtTypeReference<?> numberType;
    private final CtTypeReference<?> timeUnitType;
    private final CtTypeReference<?> durationType;

    public LiteralMultiplierOp() {
        super();

        this.numberType = this.mutatorComposite.factory.Type().createReference(Number.class);
        this.timeUnitType = this.mutatorComposite.factory.Type().createReference(TimeUnit.class);
        this.durationType = this.mutatorComposite.factory.Type().createReference(Duration.class);
        this.mutatorComposite.getMutators().add(new LiteralMultiplierMutator(this.mutatorComposite.getFactory()));
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();
        if (!(element instanceof CtInvocation)) return false;

        CtInvocation<?> invocation = (CtInvocation<?>) element;
        CtExecutableReference<?> exec = invocation.getExecutable();
        if (exec == null) return false;

        List<CtExpression<?>> arguments = invocation.getArguments();
        if (arguments.isEmpty()) return false;

        // Suporte direto a Thread.sleep(...) e Object.wait(...)
        if (isThreadOrObjectTiming(exec)) {
            return arguments.stream().anyMatch(this::isNumericTarget);
        }

        // Métodos fabris de Duration (ex: Duration.ofMillis(100), Duration.ofSeconds(2))
        if (isDurationFactoryMethod(exec)) {
            return arguments.stream().anyMatch(this::isNumericTarget);
        }

        // Métodos que recebem um objeto Duration já construído
        boolean hasDurationArg = arguments.stream().anyMatch(this::isDurationType);

        // Métodos que recebem um valor numérico + TimeUnit (ex: awaitTermination(5, TimeUnit.SECONDS))
        boolean hasNumericArg = arguments.stream().anyMatch(this::isNumericTarget);
        boolean hasTimeUnitArg = arguments.stream().anyMatch(this::isTimeUnitType);

        if (hasDurationArg || (hasNumericArg && hasTimeUnitArg)) {
            return true;
        }

        // Heurística genérica: Métodos com nomes de tempo (sleep, wait, await, delay) que recebem números
        String methodName = exec.getSimpleName().toLowerCase();
        return hasNumericArg && TIMING_METHOD_NAMES.stream().anyMatch(methodName::contains);
    }

    private boolean isThreadOrObjectTiming(CtExecutableReference<?> exec) {
        CtTypeReference<?> declaringType = exec.getDeclaringType();
        if (declaringType == null) return false;

        String qName = declaringType.getQualifiedName();
        String name = exec.getSimpleName();

        return ("java.lang.Thread".equals(qName) && "sleep".equals(name)) ||
               ("java.lang.Object".equals(qName) && "wait".equals(name));
    }

    private boolean isDurationFactoryMethod(CtExecutableReference<?> exec) {
        CtTypeReference<?> declaringType = exec.getDeclaringType();
        if (declaringType == null) return false;

        return "java.time.Duration".equals(declaringType.getQualifiedName()) &&
               exec.getSimpleName().startsWith("of");
    }

    private boolean isNumericTarget(CtExpression<?> arg) {
        if (arg == null) return false;

        // Permite literais ou acessos a variáveis (ex: Thread.sleep(TIMEOUT))
        if (!(arg instanceof CtLiteral) && !(arg instanceof CtVariableRead)) {
            return false;
        }

        CtTypeReference<?> type = arg.getType();
        if (type == null) return false;

        boolean isNumericType = false;

        // Trata tipos primitivos numéricos (int, long, double, float)
        if (type.isPrimitive()) {
            String name = type.getSimpleName();
            isNumericType = "long".equals(name) || "int".equals(name) || "double".equals(name) || "float".equals(name);
        } else {
            // Trata wrappers estendendo java.lang.Number (Long, Integer, Double, etc.)
            try {
                CtTypeReference<?> erased = type.getTypeErasure();
                isNumericType = erased != null && erased.isSubtypeOf(this.numberType);
            } catch (Exception e) {
                isNumericType = false;
            }
        }

        // Se não for um tipo numérico válido, já retorna falso
        if (!isNumericType) {
            return false;
        }

        // Agora extraímos o valor para verificar se é maior que 100
        Object value = extractValue(arg);
        if (value instanceof Number) {
            return ((Number) value).doubleValue() > 100.0;
        }

        return false;
    }

    // Método auxiliar para isolar a extração do valor
    private Object extractValue(CtExpression<?> arg) {
        if (arg instanceof CtLiteral) {
            return ((CtLiteral<?>) arg).getValue();
        }

        if (arg instanceof CtVariableRead) {
            var variableDecl = ((CtVariableRead<?>) arg).getVariable().getDeclaration();
            
            CtExpression<?> defaultExpr = null;
            if (variableDecl instanceof CtLocalVariable) {
                defaultExpr = ((CtLocalVariable<?>) variableDecl).getDefaultExpression();
            } else if (variableDecl instanceof CtField) {
                defaultExpr = ((CtField<?>) variableDecl).getAssignment();
            }

            if (defaultExpr instanceof CtLiteral) {
                return ((CtLiteral<?>) defaultExpr).getValue();
            }
        }

        return null;
    }

    private boolean isDurationType(CtExpression<?> arg) {
        if (arg == null || arg.getType() == null) return false;
        try {
            return arg.getType().isSubtypeOf(this.durationType);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTimeUnitType(CtExpression<?> arg) {
        if (arg == null || arg.getType() == null) return false;
        try {
            return arg.getType().isSubtypeOf(this.timeUnitType);
        } catch (Exception e) {
            return false;
        }
    }
}