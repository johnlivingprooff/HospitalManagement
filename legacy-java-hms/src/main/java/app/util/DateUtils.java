package app.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public interface DateUtils {

    static boolean isSameDayAndMonth(LocalDate a, LocalDate b) {
        return a.getMonth() == b.getMonth() && a.getDayOfMonth() == b.getDayOfMonth();
    }

    /**
     * <p>Convert <code>date</code> to {@link LocalDate} date, without the time part</p>
     *
     * @param date The date to convert
     * @return .
     */
    static LocalDate dateToLocalDate(Date date) {
        return LocalDate.of(getYear(date), date.getMonth() + 1, date.getDate());
    }

    static int getYear(Date date) {
        return 1900 + date.getYear();
    }

    static boolean SameDayAndMonth(LocalDate a, Date b) {
        return isSameDayAndMonth(a, dateToLocalDate(b));
    }

    static Date localDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    static String toString(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US));
    }

    static String duration(LocalDateTime start, LocalDateTime end) {
        ZonedDateTime _start = start.atZone(ZoneId.systemDefault());
        ZonedDateTime _end = end.atZone(ZoneId.systemDefault());
        Duration duration = Duration.between(_start, _end);
        Period period = Period.between(_start.toLocalDate(), _end.toLocalDate());

        return String.format(
                Locale.US,
                "%s, %s, %s, %s, %s",
                LocaleUtil.pluralString(period.getYears(), "year"),
                LocaleUtil.pluralString(period.getMonths(), "month"),
                LocaleUtil.pluralString(period.getDays(), "day"),
                LocaleUtil.pluralString(duration.toHoursPart(), "hour"),
                LocaleUtil.pluralString(duration.toMinutesPart(), "minute")
        );
    }

    static String duration(LocalTime start, LocalTime end) {
        Duration duration = Duration.between(start, end);
        return String.format(
                Locale.US,
                "%s, %s",
                LocaleUtil.pluralString(duration.toHoursPart(), "hour"),
                LocaleUtil.pluralString(duration.toMinutesPart(), "minutes")
        );
    }

    static boolean areDatesAtLeastTheseMinutesApart(LocalDateTime start, LocalDateTime end, long minutes) {
        // start >= (end - minutes)
        LocalDateTime tmp = end.minusMinutes(minutes);
        return start.isBefore(tmp) || start.isEqual(tmp);
    }

    static boolean isTimeEqualsOrAfter(LocalTime a, LocalTime b) {
        return a.isAfter(b) || a.equals(b);
    }

    static boolean isTimeLessThanOrEquals(LocalTime a, LocalTime b) {
        return a.isBefore(b) || a.equals(b);
    }

    static boolean isTimeWithinRange(LocalTime time, LocalTime min, LocalTime max) {
        // min >= time && time <= max
        return isTimeEqualsOrAfter(time, min) && isTimeLessThanOrEquals(time, max);
    }

    static boolean areTimesAtLeastTheseMinutesApart(LocalTime startTime, LocalTime endTime, long minutes) {
        LocalTime adjusted = endTime.minusMinutes(minutes);
        return startTime.isBefore(adjusted) || startTime.equals(adjusted);
    }

    static int age(Date dob) {
        return getYear(new Date()) - getYear(dob);
    }
}

