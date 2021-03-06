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
package ru.histone.v2.evaluator.resource.loader;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.Resource;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An abstraction for loading resource
 *
 * @author Alexey Nevinsky
 */
public interface Loader {
    /**
     * Method returns future of resource, which was load by url and params
     *
     * @param url    for loading
     * @param params of loading
     * @return loaded resource
     */
    CompletableFuture<Resource> loadResource(Context ctx, URI url, Map<String, Object> params);

    /**
     * Method returns scheme of current implementation
     *
     * @return current scheme
     */
    String getScheme();
}
