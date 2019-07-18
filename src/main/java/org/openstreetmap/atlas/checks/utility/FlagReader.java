package org.openstreetmap.atlas.checks.utility;

import static org.openstreetmap.atlas.checks.utility.GeoJsonCheckFlag.GENERATOR_KEY;
import static org.openstreetmap.atlas.checks.utility.GeoJsonCheckFlag.IDENTIFIER_KEY;
import static org.openstreetmap.atlas.checks.utility.GeoJsonCheckFlag.INSTRUCTIONS_KEY;
import static org.openstreetmap.atlas.checks.utility.GeoJsonCheckFlag.TIMESTAMP_KEY;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Read {@link CheckFlag}s serialized as geojson feature collections and convert them to
 * {@link GeoJsonCheckFlag}s.
 *
 * @author bbreithaupt
 */
public final class FlagReader
{
    /**
     * {@link JsonDeserializer} for {@link GeoJsonCheckFlag}s
     */
    public static class GeoJsonCheckFlagDeserializer implements JsonDeserializer<GeoJsonCheckFlag>
    {
        @Override
        public GeoJsonCheckFlag deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context)
        {
            final JsonObject properties = json.getAsJsonObject().get(PROPERTIES).getAsJsonObject();
            final String identifier = properties.get(IDENTIFIER_KEY).getAsString();
            final String instructions = properties.get(INSTRUCTIONS_KEY).getAsString();
            final String generator = properties.get(GENERATOR_KEY).getAsString();
            final String timestamp = properties.get(TIMESTAMP_KEY).getAsString();
            final List<JsonObject> features = Iterables
                    .asList(json.getAsJsonObject().get(FEATURES).getAsJsonArray()).stream()
                    .map(JsonElement::getAsJsonObject).collect(Collectors.toList());

            return new GeoJsonCheckFlag(identifier, instructions, generator, timestamp, features);
        }
    }

    // GsonBuilder with a GeoJsonCheckFlag adapter
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GeoJsonCheckFlag.class, new GeoJsonCheckFlagDeserializer())
            .create();

    /**
     * Convert a single {@link String} {@link CheckFlag} feature collection to a
     * {@link GeoJsonCheckFlag}.
     *
     * @param string
     *            {@link CheckFlag}s serialized as geojson feature collection {@link String}
     * @return a {@link GeoJsonCheckFlag} version of the {@link CheckFlag}
     */
    public static GeoJsonCheckFlag readFlagFromString(final String string)
    {
        return GSON.fromJson(string, GeoJsonCheckFlag.class);
    }

    private FlagReader()
    {
    }
}
