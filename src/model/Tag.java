package model;

import java.io.Serializable;

/**
 * A simple tag for photos: name and value pair.
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag t = (Tag) o;
        return name.equals(t.name) && value.equals(t.value);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + value.hashCode();
    }

    @Override
    public String toString() {
        return name + ":" + value;
    }
}
