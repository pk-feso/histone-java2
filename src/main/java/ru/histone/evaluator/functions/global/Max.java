/**
 *    Copyright 2012 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.evaluator.functions.global;

import ru.histone.evaluator.nodes.Node;
import ru.histone.evaluator.nodes.NumberNode;
import ru.histone.utils.ArrayUtils;

/**
 * Return maximum value from specified arguments<br/>
 * When calculating max value, all arguments are casted to number type
 */
public class Max implements GlobalFunction {
    @Override
    public String getName() {
        return "max";
    }

    @Override
    public Node execute(Node... args) {
        if (ArrayUtils.isEmpty(args)) {
            return Node.UNDEFINED;
        }
        NumberNode result = null;
        for (Node arg : args) {
            if (!arg.isNumber()) {
                continue;
            }
            NumberNode argNum = (NumberNode) arg;
            result = (result == null || result.compareTo(argNum) < 0) ? argNum : result;
        }
        return result == null ? Node.UNDEFINED : result;
    }
}