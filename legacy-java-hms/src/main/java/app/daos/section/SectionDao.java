package app.daos.section;

import app.models.section.Section;
import app.models.section.SectionMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(SectionMapper.class)
public interface SectionDao {

    @SqlQuery("SELECT S.*, CONCAT_WS(' ', U.FirstName, U.LastName) AS SectionHead, " +
            "(SELECT COUNT(Id) FROM Users WHERE SectionID = S.Id GROUP BY SectionID) As UserCount " +
            "FROM Section S " +
            "LEFT JOIN SectionHead SH ON SH.SectionID = S.Id " +
            "LEFT JOIN Users U ON U.Id = SH.UserID " +
            "WHERE S.Hidden = False")
    List<Section> getVisibleSections();

    @SqlQuery("SELECT * FROM Section WHERE Id = :id")
    Section getSection(@Bind("id") long sectionID);
}
