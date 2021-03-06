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

package ru.histone.v2.evaluator.node;

import ru.histone.v2.rtti.HistoneType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
public class DateEvalNode extends MapEvalNode {

    public DateEvalNode(Map<String, EvalNode> value) {
        super(value);
    }

    @Override
    public List<HistoneType> getAdditionalTypes() {
        return Collections.singletonList(HistoneType.T_DATE);
    }

    @Override
    public boolean hasAdditionalType(HistoneType type) {
        return type == HistoneType.T_DATE;
    }
}
