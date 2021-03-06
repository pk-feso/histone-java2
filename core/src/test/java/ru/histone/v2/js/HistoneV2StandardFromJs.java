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

package ru.histone.v2.js;

import com.google.common.base.Charsets;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Gali Alykoff
 */
public class HistoneV2StandardFromJs {
    public static final String RESOURCE_PATH_TO_RUNNER = "resources/v2/tools/histone2_js_runner.js";
    public static final String RELATIVE_RESOURCE_PATH_TO_RUNNER = "src/test/" + RESOURCE_PATH_TO_RUNNER;
    private static final String PATH_TO_HISTONE2_JS_RUNNER = getRunnerJsAbsolutePath(RESOURCE_PATH_TO_RUNNER, RELATIVE_RESOURCE_PATH_TO_RUNNER);

    public static void main(String[] args) throws IOException {
        final String baseURI = "";
        final String tpl = "{{'ab$1safsd$1sdfsdf1sdfsdf1dfdfs'->split('$1')->toJSON}}";

        //a {{2.9870654321 -> toFixed(8)}} b getDaysInMonth
        //"{{var x = {{#{{var ui = require('static:///script/ui/ui.tpl')}}{{var info = ui.getPipe('lk/main/info')}}{{info->toJSON}}#}}}}{{x}}";
        System.out.println(getNodes(tpl));
        System.out.println(getTpl(tpl));
        System.out.println("--------");

        try {
            ExecutorService executor = Executors.newFixedThreadPool(20);
            final Converter converter = new Converter();
            final Evaluator evaluator = new Evaluator(converter);
            final Parser parser = new Parser();
            RunTimeTypeInfo rtti = new RunTimeTypeInfo(executor, new SchemaResourceLoader(), evaluator, parser);

            final Context context = Context.createRoot("", rtti, new DefaultPropertyHolder());
            final ExpAstNode root = parser.process(tpl, baseURI);
            System.out.println(AstJsonProcessor.write(root));
            final String result = evaluator.process(root, context);
            System.out.println(result);
            executor.shutdown();
        } catch (HistoneException e) {
            e.printStackTrace();
        }
    }

    public static String getNodes(String tpl) throws IOException {
        tpl = tpl.replaceAll("\"", "\"");
        final ProcessBuilder processBuilder = new ProcessBuilder(
                "/usr/local/bin/node", PATH_TO_HISTONE2_JS_RUNNER, "--tpl=\"" + tpl + "\"", "-n"
        );
        return getProcessResult(processBuilder);
    }

    public static String getTpl(String tpl) throws IOException {
        tpl = tpl.replaceAll("\"", "\"");
        final ProcessBuilder processBuilder = new ProcessBuilder(
                "/usr/local/bin/node", PATH_TO_HISTONE2_JS_RUNNER, "--tpl=\"" + tpl + "\""
        );
        return getProcessResult(processBuilder);
    }

    private static String getProcessResult(ProcessBuilder builder) throws IOException {
        final Process process = builder.start();
        try (
                final InputStream is = process.getInputStream();
                final InputStreamReader inputStreamReader = new InputStreamReader(is, Charsets.UTF_8);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            String line;
            StringBuilder processResult = new StringBuilder("");
            while ((line = bufferedReader.readLine()) != null) {
                processResult.append(line);
            }
            return processResult.toString();
        }
    }

    private static String getRunnerJsAbsolutePath(String resourcePath, String relativePath) {
        final URL uriScript = MethodHandles
                .lookup()
                .lookupClass()
                .getClassLoader()
                .getResource(resourcePath);
        try {
            if (uriScript == null) {
                return Paths.get(RELATIVE_RESOURCE_PATH_TO_RUNNER).toAbsolutePath().toString();
            } else {
                return Paths.get(uriScript.toURI()).toAbsolutePath().toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("histone2 js runner script not found", e);
        }
    }
}
