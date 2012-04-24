package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.DictionaryEntry;
import net.cherrytools.runtime.DictionaryEntry;
import net.cherrytools.runtime.ZemObject;

public class DictionaryEntryNode extends Node {
    private Node key;
    private Node value;

    public DictionaryEntryNode(SourcePosition pos, Node key, Node value) {
        super(pos);
        this.key = key;
        this.value = value;
    }

    public Node getKey() {
        return key;
    }

    public Node getValue() {
        return value;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        return new DictionaryEntry(key.eval(interpreter), value.eval(interpreter));
    }

    @Override
    public String toString() {
        return "(" + key.toString() + " " + value.toString() + ")";
    }
}
