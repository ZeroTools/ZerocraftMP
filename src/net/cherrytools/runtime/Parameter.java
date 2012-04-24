package net.cherrytools.runtime;

public class Parameter {
    private String name;
    private ZemObject value;

    public Parameter(String name, ZemObject value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ZemObject getDefaultValue() {
        return value;
    }
}
