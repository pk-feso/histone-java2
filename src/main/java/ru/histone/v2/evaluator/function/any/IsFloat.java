/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.DoubleEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class IsFloat extends AbstractFunction {
    @Override
    public String getName() {
        return "isFloat";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (node instanceof DoubleEvalNode) {
            if (EvalUtils.canBeLong(((DoubleEvalNode) node).getValue())) {
                return EvalUtils.getValue(false);
            }
            return EvalUtils.getValue(true);
        }
        return EvalUtils.getValue(false);
    }
}
