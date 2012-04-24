package net.cherrytools.language;

import net.cherrytools.runtime.Dictionary;
import net.cherrytools.runtime.ZemArray;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemNumber;
import net.cherrytools.runtime.ZemString;

/**
 * Types don't match.
 *
 * @author <a href="mailto:grom@zeminvaders.net">Cameron Zemek</a>
 */
public class TypeMismatchException extends ZemException {
    private static final long serialVersionUID = 9115378805326306069L;

    static private String toString(Class type) {
        if (type == Dictionary.class) {
            return "dictionary";
        } else if (type == ZemArray.class) {
            return "array";
        } else if (type == ZemBoolean.class) {
            return "boolean";
        } else if (type == ZemNumber.class) {
            return "number";
        } else if (type == ZemString.class) {
            return "string";
        } else {
            return type.getName();
        }
    }

    public TypeMismatchException(SourcePosition pos, Class expect, Class actual) {
        super("Type mismatch - Excepted type '" + toString(expect) + "' but got type '" + toString(actual) + "'", pos);
    }
}
