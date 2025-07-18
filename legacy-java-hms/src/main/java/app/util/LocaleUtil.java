package app.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

public interface LocaleUtil {
    static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isBlank();
    }

    static boolean isStringLengthWithinRange(String input, int min, int max) {
        return withinBounds(isNullOrEmpty(input) ? 0 : input.length(), min, max);
    }

    static boolean withinBounds(int value, int min, int max) {
        return min <= value && value <= max;
    }

    /**
     * <p>Check whether the given input contains only alpha characters and spaces</p>
     *
     * @param input Text to check+
     * @return
     */
    static boolean isAlphaText(String input) {
        return input.matches("^[aA-zZ\\x20]+$");
    }

    static String stripAllNonAlphanumerics(String input) {
        if (!isNullOrEmpty(input)) {
            return input.replaceAll("[^a-zA-Z0-9]", "");
        }
        return null;
    }

    static boolean isAlphaNumericText(String input) {
        return input.matches("^[\\w\\s]+$");
    }

    /**
     * <p>Remove spaces and convert each character after the space to upper case</p>
     * <p>Example</p>
     * <code>
     * "Hello world", "hello world", "hello World" => "HelloWorld"
     * </code>
     *
     * @param input The string to transform
     * @return .
     */
    static String ucString(String input) {
        String[] parts = input.trim().split("\\s");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
        }
        return sb.toString();
    }

    static String capitalize(String input) {
        String[] parts = input.toLowerCase().trim().split("\\s");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i], 1, parts[i].length());
        }
        return sb.toString();
    }

    static <T extends Number> T convert(String input, Class<T> clazz) throws Exception {
        try {
            return clazz.cast(clazz.getMethod("valueOf", String.class).invoke(null, input));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * <p>Check whether <code>a</code> is after <code>b</code></p>
     *
     * @param a Start date
     * @param b End date
     * @return .
     * @see Date#after(Date)
     */
    static boolean isDateAfter(Date a, Date b) {
        return a.after(b);
    }

    static String pluralString(Number count, String label) {
        return pluralString(count, label, 0);
    }

    static String pluralString(Number count, String label, int precision) {
        final String format = "%,." + precision + "f %s";
        count = Math.abs(count.doubleValue());
        if (count.doubleValue() == 1) return String.format(Locale.US, format, count, label);
        return String.format(Locale.US, format + "s", count, label);
    }

    static String getFileExtension(String name) {
        final int index = name.lastIndexOf('.');
        if (index != -1) {
            return name.substring(index + 1).trim();
        }
        return null;
    }

    static String getFileExtensionWithPeriod(String name) {
        String ext = getFileExtension(name);
        if (ext != null) {
            return "." + ext;
        }
        return null;
    }

    static String stripNonFileNameSymbols(String name) {
        return name.replaceAll("[^a-zA-Z0-9.~!\\-+_@#$%^&()\\[\\]{},]", "_");
    }

    static LocalDate dateToLocalDate(Date date) {
        // Twig resolves all Date instances to java.sql.Date, which causes an UnsupportedOperationException
        // So this was the best solution (or the Calendar way)
        return LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
    }

    static String getTimePeriod(Date start, Date end) {
        final Period period = calculateTimePeriod(start, end);
        String str = null;

        if (period.getYears() > 0) {
            str = pluralString(period.getYears(), "year");
        }

        if (period.getMonths() > 0) {
            if (str != null) {
                str += ", ";
            } else {
                str = "";
            }
            str += pluralString(period.getMonths(), "month");
        }

        if (period.getDays() > 0) {
            if (str != null) {
                str += ", ";
            } else {
                str = "";
            }
            str += pluralString(period.getDays(), "day");
        }

        return str;
    }

    static Period calculateTimePeriod(Date start, Date end) {
        return Period.between(LocaleUtil.dateToLocalDate(start), LocaleUtil.dateToLocalDate(end));
    }

    static int getTotalMonthsBetweenDates(Date start, Date end) {
        final Period period = calculateTimePeriod(start, end);
        return (period.getYears() * 12) + period.getMonths();
    }

    static boolean stringsMatch(String a, String b) {
        if (isNullOrEmpty(a) || isNullOrEmpty(b)) return false;
        return a.equalsIgnoreCase(b);
    }

    static String formatDate(Date date, boolean includeTime) {
        String pattern = "MMM d, yyyy";
        if (includeTime) {
            pattern += " HH:mm";
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    static String formatDate(LocalDateTime date) {
        return formatDate(date, true);
    }

    static String formatDate(LocalDateTime date, boolean withTime) {
        String pattern = "MMM d, yyyy";
        if (withTime) {
            pattern += " HH:mm";
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    static String formatDate(ZonedDateTime date) {
        String pattern = "MMM d, yyyy HH:mm";
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    static String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    static String formatDate(LocalDate date) {
        return String.format(Locale.US, "%s %02d, %d",
                date.getMonth().getDisplayName(TextStyle.SHORT, Locale.US),
                date.getDayOfMonth(),
                date.getYear()
        );
    }


    /**
     * <p>Compares only the date values. The time portion is ignored</p>
     *
     * @param start Start date
     * @param end   End date
     * @return true if <code>start</code> is the same as or after <code>end</code>
     */
    static boolean isDateEqualToOrAfter(Date start, Date end) {
        return compareDatesOnly(start, end) >= 0;
    }

    static boolean isDateOnlyAfter(Date start, Date end) {
        return compareDatesOnly(start, end) == 1;
    }

    static int compareDatesOnly(Date a, Date b) {
        LocalDate lda = dateToLocalDate(a);
        LocalDate ldb = dateToLocalDate(b);
        return lda.compareTo(ldb);
    }

    /**
     * @param value Teh value to test
     * @param min   Minimum value
     * @param max   Maximum value
     * @return True if <code>value</code> is inclusively within range of <code>min</code> and <code>max</code>
     */
    static boolean isNumberWithinRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Convenient wrapper constructor for {@link Date#Date(int, int, int)}
     *
     * @param year  The year. The method will automatically subtract 1900 from this date
     * @param month The month between 0-11
     * @param date  Date between 1-31
     * @return new {@link Date} object
     * @see Date
     */
    static Date newDate(int year, int month, int date) {
        return new Date(year - 1900, month, date);
    }

    /**
     * <p>Scales down the date to the first day of the month represented by the date</p>
     *
     * @param date Date
     * @return .
     */
    static Date toFirstDayOfMonth(Date date) {
        Date newDate = new Date(date.getTime());
        newDate.setDate(1); // First day of month
        return newDate;
    }

    static String dateToIsoString(LocalDate now) {
        return String.format(Locale.US, "%d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    static String timeToString(LocalTime now) {
        return String.format(Locale.US, "%02d:%02d", now.getHour(), now.getMinute());
    }

    static String dec2str(BigDecimal decimal) {
        return new DecimalFormat("#,##0.00").format(decimal);
    }
}