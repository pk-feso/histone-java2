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

package ru.histone.v2.parser.node;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public enum AstRegexType {
    RE_GLOBAL(0x01),
    RE_MULTILINE(0x02),
    RE_IGNORECASE(0x04);

    private final int id;

    AstRegexType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
