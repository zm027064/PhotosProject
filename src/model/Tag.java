package model;

import java.io.Serializable;

/**
 * A simple tag for photos: name and value pair.
 *
 * <p>Represents metadata attached to a photo (for example
 * location:Paris or person:Alice).</p>
 *
 * @author Prayrit
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;
    /**
     * Construct a tag with a name and value.
     *
     * @param name tag name/type
     * @param value tag value
     */
    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return tag name
     */
    public String getName() { return name; }

    /**
     * @return tag value
     */
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag t = (Tag) o;
        return name.equals(t.name) && value.equals(t.value);
    }

    /**
     * Hash code consistent with {@link #equals(Object)}.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return name.hashCode() * 31 + value.hashCode();
    }

    /**
     * String form `name:value`.
     *
     * @return a short textual representation of the tag
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }
}
