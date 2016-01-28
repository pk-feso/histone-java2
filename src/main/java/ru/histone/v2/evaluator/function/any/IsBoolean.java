package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class IsBoolean implements Function {
    @Override
    public String getName() {
        return "isBoolean";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        boolean res = args.get(0) instanceof BooleanEvalNode;
        return EvalUtils.getValue(res);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}