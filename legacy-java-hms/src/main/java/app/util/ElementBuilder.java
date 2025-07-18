package app.util;

import java.util.*;

public class ElementBuilder {
    private String text;
    private boolean close;
    private final String tag;
    private boolean singleTag;
    private List<ElementBuilder> children;
    private Map<String, String> attributes;
    private boolean textBeforeChildren = true;

    public ElementBuilder(String name) {
        this(name, true);
    }

    public ElementBuilder(String name, boolean close) {
        this.tag = name;
        this.close = close;
        attributes = new LinkedHashMap<>();
    }

    public <T extends ElementBuilder> T single() {
        this.singleTag = true;
        this.close = false;
        return (T) this;
    }

    public <T extends ElementBuilder> T paired() {
        this.singleTag = false;
        return (T) this;
    }

    public <T extends ElementBuilder> T attribute(String name, Object value) {
        attributes.put(name, Objects.toString(value));
        return (T) this;
    }

    public <T extends ElementBuilder> T addChild(ElementBuilder e) {
        if (children == null) {
            children = new LinkedList<>();
        }
        children.add(e);
        return (T) this;
    }

    public <T extends ElementBuilder> T removeAttribute(String name) {
        attributes.remove(name);
        return (T) this;
    }

    public <T extends ElementBuilder> T text(String text) {
        return text(text, true);
    }

    public <T extends ElementBuilder> T text(String text, boolean beforeChildren) {
        this.text = text;
        this.textBeforeChildren = beforeChildren;
        return (T) this;
    }

    public <T extends ElementBuilder> T close(boolean close) {
        this.close = close;
        return (T) this;
    }

    public <T extends ElementBuilder> T classes(String classlist) {
        return attribute("class", classlist);
    }

    public <T extends ElementBuilder> T autofocus() {
        return attribute("autofocus", "autofocus");
    }

    public <T extends ElementBuilder> T id(String id) {
        return attribute("id", id);
    }

    public <T extends ElementBuilder> T name(String name) {
        return attribute("name", name);
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder("<").append(tag);
        for (String name : attributes.keySet()) {
            stringBuilder.append(' ')
                    .append(name).append('=').append('"').append(attributes.get(name)).append('"');
        }
        if (singleTag) {
            stringBuilder.append(" /");
        }
        stringBuilder.append('>');
        if (textBeforeChildren) {
            if (!LocaleUtil.isNullOrEmpty(text)) {
                stringBuilder.append(text);
            }
        }
        if (children != null) {
            for (ElementBuilder child : children) {
                stringBuilder.append(child.build());
            }
        }
        if (!textBeforeChildren) {
            if (!LocaleUtil.isNullOrEmpty(text)) {
                stringBuilder.append(text);
            }
        }
        if (close) {
            if (singleTag) {
                throw new IllegalArgumentException("Single tag " + tag + " cannot be closed.");
            }
            stringBuilder.append("</").append(tag).append('>');
        }
        return stringBuilder.toString();
    }

    public static class FormBuilder extends ElementBuilder {
        public FormBuilder() {
            super("form", false);
        }

        public FormBuilder target(String url) {
            return (FormBuilder) attribute("action", url);
        }

        public FormBuilder method(String method) {
            return (FormBuilder) attribute("method", method.toUpperCase());
        }

        public FormBuilder enctype(String type) {
            return (FormBuilder) attribute("enctype", type);
        }

        public FormBuilder charset(String charset) {
            return (FormBuilder) attribute("accept-charset", charset.toUpperCase());
        }
    }

    public static class Input extends ElementBuilder {

        public Input() {
            super("input", false);
            single();
        }

        public Input type(String type) {
            return attribute("type", type);
        }

        public Input required() {
            return attribute("required", "required");
        }

        public Input optional() {
            return removeAttribute("required");
        }
    }


    public static class Anchor extends ElementBuilder {

        public Anchor() {
            super("a");
        }

        public Anchor(boolean close) {
            super("a", close);
        }

        public Anchor href(String link) {
            return (Anchor) attribute("href", link);
        }
    }
}
