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

package ru.histone.v2.parser.tokenizer;

import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alexey Nevinsky on 12.01.2016.
 */
public class TokenizerResult {
    private List<Token> tokens;
    private boolean found = false;

    public TokenizerResult(Token... tokens) {
        found = tokens != null;
        if (tokens != null) {
            this.tokens = Arrays.asList(tokens);
        }
    }

    public TokenizerResult(List<Token> tokens) {
        found = CollectionUtils.isNotEmpty(tokens);
        if (tokens != null) {
            this.tokens = tokens;
        }
    }

    public TokenizerResult(boolean found) {
        this.found = found;
    }

    public boolean isFound() {
        return found;
    }

    public Token first() {
        return tokens.get(0);
    }

    public String firstValue() {
        return tokens.get(0).getValue();
    }
}
