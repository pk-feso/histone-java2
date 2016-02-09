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

package ru.histone.v2;

import org.junit.Test;
import ru.histone.HistoneException;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author alexey.nevinsky
 */
public class ConcreteTest extends BaseTest {
    @Test
    public void concreteTest() throws HistoneException {
        RunTimeTypeInfo rtti = new RunTimeTypeInfo(Executors.newFixedThreadPool(20));

        HistoneTestCase.Case testCase = new HistoneTestCase.Case();
        testCase.setExpectedResult("a 0.0000010003 b");
        testCase.setContext(getMap());
//        testCase.setExpectedAST("[31,[25,[2,\"ab+c\",0],\"re\"],[24,[22,[21,\"re\"],\"test\"],\"ac\"]]");
        doTest("a {{-.123 -> isUndefined()}} b", rtti, testCase);
    }

    private Map<String, Object> getMap() {
        Map<String, Object> res = new HashMap<>();

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("foo", 1L);
        values.put("bar", 2L);
        values.put("y", 3L);

        res.put("this", values);
        return res;
    }
}
