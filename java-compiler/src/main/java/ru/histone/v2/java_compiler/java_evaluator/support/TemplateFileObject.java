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

package ru.histone.v2.java_compiler.java_evaluator.support;

import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author Alexey Nevinsky
 */
public class TemplateFileObject extends SimpleJavaFileObject {

    public static final String JAVA_EXTENSION = ".java";

    private final CharSequence source;
    private ByteArrayOutputStream byteCode;

    TemplateFileObject(final String className, final CharSequence source) {
        super(URI.create(className + JAVA_EXTENSION), Kind.SOURCE);
        this.source = source;
    }

    TemplateFileObject(final String className, final Kind kind) {
        super(URI.create(className + JAVA_EXTENSION), kind);
        source = null;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        if (source == null)
            throw new UnsupportedOperationException("getCharContent()");
        return source;
    }

    /**
     * Return an input stream for reading the byte code
     *
     * @see SimpleJavaFileObject#openInputStream()
     */
    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(getByteCode());
    }

    /**
     * Return an output stream for writing the bytecode
     *
     * @see SimpleJavaFileObject#openOutputStream()
     */
    @Override
    public OutputStream openOutputStream() {
        byteCode = new ByteArrayOutputStream();
        return byteCode;
    }

    /**
     * @return the byte code generated by the compiler
     */
    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }

}
