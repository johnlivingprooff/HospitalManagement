package app.models.doctor.schedule;

import app.core.annotations.HtmlFieldDisplay;
import app.util.LocaleUtil;

import java.time.LocalDate;


@HtmlFieldDisplay(value = "getId()")
public class ScheduledDays {
    private long id;
    private LocalDate date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return LocaleUtil.formatDate(date);
    }
}
