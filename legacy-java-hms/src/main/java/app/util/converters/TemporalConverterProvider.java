package app.util.converters;

import com.google.auto.service.AutoService;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConvertersProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@AutoService(ConvertersProvider.class)
public class TemporalConverterProvider implements ConvertersProvider {
    @Override
    public void fill(Map<Class<?>, Converter<?>> map) {
        map.put(LocalDate.class, new TemporalConverter<LocalDate>(TemporalConverter.Which.date));
        map.put(LocalTime.class, new TemporalConverter<LocalTime>(TemporalConverter.Which.time));
        map.put(LocalDateTime.class, new TemporalConverter<LocalDateTime>(TemporalConverter.Which.date_time));
    }
}