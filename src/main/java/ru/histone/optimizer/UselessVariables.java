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
package ru.histone.optimizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ru.histone.HistoneException;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.AstNodeType;
import ru.histone.utils.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * User: sazonovkirill@gmail.com
 * Date: 09.01.13
 */
public class UselessVariables extends BaseOptimization {
    private Mode mode;
    private Set<String> selectors;

    public UselessVariables(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    public ArrayNode removeUselessVariables(ArrayNode ast) throws HistoneException {
        selectors = new HashSet<String>();
        mode = Mode.COLLECT_SELECTORS;
        process(ast);
        mode = Mode.REMOVE_VARIABLES;
        return process(ast);
    }

    protected JsonNode processSelector(ArrayNode selector) throws HistoneException {
        JsonNode fullVariable = selector.get(1);

        JsonNode firstToken = fullVariable.get(0);
        if (firstToken.isTextual()) {
            String varName = firstToken.asText();
            selectors.add(varName);
        } else {
            processAstNode(firstToken);
        }

        for (int i = 1; i < fullVariable.size(); i++) {
            JsonNode t = fullVariable.get(i);
            if (t.isTextual()) {
                // nothing
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.STRING) {
                // nothing
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.INT) {
                // nothing
            } else {
                processAstNode(t);
            }
        }

        return selector;
    }

    protected JsonNode processVariable(ArrayNode variable) throws HistoneException {
        Assert.isTrue(variable.size() == 3);

        JsonNode var = variable.get(1);
        JsonNode value = variable.get(2);
        JsonNode processedValue = processAstNode(value);

        String varName = var.asText();
        if (selectors.contains(varName)) {
            return nodeFactory.jsonArray(AstNodeType.VAR, var, processedValue);
        } else {
            return nodeFactory.jsonString("");
        }
    }

    enum Mode {
        COLLECT_SELECTORS,
        REMOVE_VARIABLES
    }

    @Override
    public void pushContext() {
        // There is no context to push in this optimizer
    }

    @Override
    public void popContext() {
        // There is no context to pop in this optimizer
    }
}
