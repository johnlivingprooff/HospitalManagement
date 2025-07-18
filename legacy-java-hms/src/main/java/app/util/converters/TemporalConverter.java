package app.util.converters;

import org.postgresql.util.PGTime;
import org.postgresql.util.PGTimestamp;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Locale;

public class TemporalConverter<T extends Temporal> implements Converter<T> {
    enum Which {
        time, date, date_time
    }

    private final Which which;

    TemporalConverter(Which which) {
        this.which = which;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(Object o) throws ConverterException {
        if (o instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) o;
            ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault());
            switch (which) {
                case date:
                    return (T) zonedDateTime.toLocalDate();
                case time:
                    return (T) zonedDateTime.toLocalTime();
                case date_time:
                    return (T) zonedDateTime.toLocalDateTime();
            }
        }
        if (o == null) {
            return null;
        }
        return (T) throwUnsupportedTypeException(o.getClass());
    }

    @Override
    public Object toDatabaseParam(T t) {
        if (t == null) {
            return null;
        }
        switch (which) {
            case date_time:
                return PGTimestamp.valueOf(((LocalDateTime) t));
            case time:
                return PGTime.valueOf((LocalTime) t);
            case date:
                return PGTimestamp.valueOf(((LocalDate) t).atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());
        }
        return throwUnsupportedTypeException(t.getClass());
    }

    private Object throwUnsupportedTypeException(Class<?> clazz) {
        throw new IllegalArgumentException(String.format(Locale.US, "Unsupported type %s.", clazz));
    }
}
