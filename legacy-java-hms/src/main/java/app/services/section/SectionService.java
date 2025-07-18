package app.services.section;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.section.SectionDao;
import app.models.section.Section;

import java.util.List;

@ServiceDescriptor
public final class SectionService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public SectionService(Configuration configuration) {
        super(configuration);
    }

    public List<Section> getVisibleSections() {
        return withDao(SectionDao.class).getVisibleSections();
    }

    public Section getSection(long sectionID) {
        return withDao(SectionDao.class).getSection(sectionID);
    }
}
