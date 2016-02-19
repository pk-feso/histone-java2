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

package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.function.any.*;
import ru.histone.v2.evaluator.function.array.*;
import ru.histone.v2.evaluator.function.global.*;
import ru.histone.v2.evaluator.function.macro.MacroBind;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.function.macro.MacroExtend;
import ru.histone.v2.evaluator.function.macro.RequireCall;
import ru.histone.v2.evaluator.function.number.*;
import ru.histone.v2.evaluator.function.regex.Test;
import ru.histone.v2.evaluator.function.string.*;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.NullEvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static ru.histone.v2.rtti.HistoneType.*;

/**
 * RTTI used to storing {@link Function} for global and default types and user defined functions. Create it ones and
 * use it as parameter to create a {@link Context}.
 *
 * @author gali.alykoff on 22/01/16.
 */
public class RunTimeTypeInfo implements Irtti, Serializable {
    private final Map<HistoneType, Map<String, Function>> userTypes = new ConcurrentHashMap<>();
    private final Map<HistoneType, Map<String, Function>> typeMembers = new ConcurrentHashMap<>();

    private final Executor executor;
    private final HistoneResourceLoader loader;

    public RunTimeTypeInfo(Executor executor, HistoneResourceLoader loader) {
        this.executor = executor;
        this.loader = loader;

        for (HistoneType type : HistoneType.values()) {
            typeMembers.put(type, new HashMap<>());
            userTypes.put(type, new HashMap<>());
        }

        registerCommonFunctions();
    }

    private void registerCommonFunctions() {
        registerForAlltypes(new ToJson());
        registerForAlltypes(new ToString());
        registerForAlltypes(new ToBoolean());
        registerForAlltypes(new ToNumber());
        registerForAlltypes(new IsUndefined());
        registerForAlltypes(new IsNull());
        registerForAlltypes(new IsBoolean());
        registerForAlltypes(new IsNumber());
        registerForAlltypes(new IsInt());
        registerForAlltypes(new IsFloat());;
        registerForAlltypes(new IsString());
        registerForAlltypes(new IsArray());
        registerForAlltypes(new IsMacro());

        registerCommon(T_NUMBER, new ToAbs());
        registerCommon(T_NUMBER, new ToCeil());
        registerCommon(T_NUMBER, new ToChar());
        registerCommon(T_NUMBER, new ToFloor());
        registerCommon(T_NUMBER, new ToRound());
        registerCommon(T_NUMBER, new ToFixed());

        registerCommon(T_ARRAY, new ArrayLength());
        registerCommon(T_ARRAY, new Keys(true));
        registerCommon(T_ARRAY, new Keys(false));
        registerCommon(T_ARRAY, new Reverse());
        registerCommon(T_ARRAY, new ArrayMap());
        registerCommon(T_ARRAY, new ArrayFilter());
        registerCommon(T_ARRAY, new ArraySome());
        registerCommon(T_ARRAY, new ArrayEvery());
        registerCommon(T_ARRAY, new ArrayJoin());
        registerCommon(T_ARRAY, new ArrayChunk());
        registerCommon(T_ARRAY, new ArrayReduce());
        registerCommon(T_ARRAY, new ArrayFind());
        registerCommon(T_ARRAY, new ArrayForEach());
        registerCommon(T_ARRAY, new ArrayGroup());
        registerCommon(T_ARRAY, new ArraySort());
        registerCommon(T_ARRAY, new ArraySlice());
        registerCommon(T_ARRAY, new ArrayHtmlEntities());

        registerCommon(T_GLOBAL, new Range());
        registerCommon(T_GLOBAL, new LoadJson(executor, loader));
        registerCommon(T_GLOBAL, new LoadText(executor, loader));
        registerCommon(T_GLOBAL, new GetBaseUri());
        registerCommon(T_GLOBAL, new GetUniqueId());
        registerCommon(T_GLOBAL, new ResolveURI());
        registerCommon(T_GLOBAL, new GetWeekDayName(true));
        registerCommon(T_GLOBAL, new GetWeekDayName(false));
        registerCommon(T_GLOBAL, new GetMonthName(true));
        registerCommon(T_GLOBAL, new GetMonthName(false));
        registerCommon(T_GLOBAL, new GetRand());
        registerCommon(T_GLOBAL, new GetMinMax(false));
        registerCommon(T_GLOBAL, new GetMinMax(true));
        registerCommon(T_GLOBAL, new GetDate());
        registerCommon(T_GLOBAL, new GetDayOfWeek());
        registerCommon(T_GLOBAL, new GetDaysInMonth());
        registerCommon(T_GLOBAL, new Require());

        registerCommon(T_REGEXP, new Test());

        registerCommon(T_STRING, new StringLength());
        registerCommon(T_STRING, new Case(false));
        registerCommon(T_STRING, new Case(true));
        registerCommon(T_STRING, new StringHtmlEntities());
        registerCommon(T_STRING, new StringCharCodeAt());
        registerCommon(T_STRING, new StringReplace());
        registerCommon(T_STRING, new StringSlice());
        registerCommon(T_STRING, new StringSplit());
        registerCommon(T_STRING, new StringStrip());

        registerCommon(T_MACRO, new MacroCall());
        registerCommon(T_MACRO, new MacroBind());
        registerCommon(T_MACRO, new MacroExtend());

        registerCommon(T_REQUIRE, new RequireCall());
    }

    private void registerForAlltypes(Function function) {
        for (HistoneType type : HistoneType.values()) {
            registerCommon(type, function);
        }
    }

    private void registerCommon(HistoneType type, Function function) {
        typeMembers.get(type).put(function.getName(), function);
    }

    @Override
    public Optional<Function> getFunc(HistoneType type, String funcName) {
        Function f = userTypes.get(type).get(funcName);
        if (f == null) {
            f = typeMembers.get(type).get(funcName);
            if (f == null) {
//                throw new FunctionExecutionException("Couldn't find function '" + funcName + "' for type " + type);
                return Optional.empty();
            }
        }
        return Optional.of(f);
    }

    @Override
    public void register(HistoneType type, String funcName, Function func) {
        throw new NotImplementedException();
    }

    @Override
    public void unregistered(HistoneType type, String funcName) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(Context context, HistoneType type, String funcName, List<EvalNode> args) {
        final Optional<Function> fRaw = getFunc(type, funcName);
        if (!fRaw.isPresent()) {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }
        final Function f = fRaw.get();
        if (f.isAsync()) {
            // TODO it should be more compact
            return CompletableFuture
                    .completedFuture(null)
                    .thenComposeAsync((x) -> f.execute(context, args), executor);
        }
        return f.execute(context, args);
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(Context context, EvalNode node, String funcName, List<EvalNode> args) {
        return callFunction(context, node.getType(), funcName, args);
    }
}
