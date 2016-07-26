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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.histone.v2.support.CasePack;
import ru.histone.v2.support.StringParamResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky on 25.12.2015.
 */
@ExtendWith(StringParamResolver.class)
@CasePack("simple")
public class EvaluatorTest extends HistoneTest {

    @TestFactory
    @Override
    public Stream<DynamicTest> loadCases(String param) throws IOException, URISyntaxException {
        return super.loadCases(param);
    }
}
