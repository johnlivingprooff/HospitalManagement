package app.models.doctor.schedule;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ScheduleSerializer implements JsonSerializer<Schedule> {

    @Override
    public JsonElement serialize(Schedule schedule, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("id", schedule.getId());
        object.addProperty("date", schedule.toString());
        return object;
    }
}
