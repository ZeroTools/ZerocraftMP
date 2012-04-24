/*
 * Copyright (c) 2008 Cameron Zemek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

/**
 * A function callable by the interpreter.
 *
 * @author <a href="mailto:grom@zeminvaders.net">Cameron Zemek</a>
 */
public abstract class Function extends ZemObject {
    /**
     * Get the number of parameters to this function.
     *
     * @return Number of parameters
     */
    abstract public int getParameterCount();

    /**
     * Get the name of the parameter
     *
     * @param index Parameter index
     * @return The name of the parameter
     */
    abstract public String getParameterName(int index);

    /**
     * Get the default value of the functions parameter.
     *
     * @param index Parameter index
     * @return The default value of the parameter. Return null if no default.
     */
    abstract public ZemObject getDefaultValue(int index);

    /**
     * Evaluate the function.
     *
     * @param interpreter
     * @param pos Source position of function call
     * @return The result of evaluating the function.
     */
    abstract public ZemObject eval(Interpreter interpreter, SourcePosition pos);

    @Override
    public int compareTo(ZemObject o) {
        throw new UnsupportedOperationException();
    }
}
