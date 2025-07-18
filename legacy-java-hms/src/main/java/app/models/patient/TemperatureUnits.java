package app.models.patient;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "symbol", value = "name()")
public enum TemperatureUnits {
    Celsius("℃"),
    Fahrenheit("℉");

    TemperatureUnits(String symbol) {
        this.symbol = symbol;
    }

    public final String symbol;

    public static final TemperatureUnits[] UNITS = values();
}
