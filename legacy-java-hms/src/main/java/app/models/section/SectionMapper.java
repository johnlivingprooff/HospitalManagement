package app.models.section;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public final class SectionMapper extends ReflectionBeanMapper<Section> {

    public SectionMapper() {
        super(Section.class);
    }
}
