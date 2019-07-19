package org.openstreetmap.atlas.checks.utility;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.TYPE;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonType.FEATURE_COLLECTION;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * A simplified representation of a {@link CheckFlag}. Flag features are stored as
 * geo{@link JsonObject}s.
 *
 * @author bbreithaupt
 */
public class GeoJsonCheckFlag implements Serializable
{
    protected static final String IDENTIFIER_KEY = "id";
    protected static final String INSTRUCTIONS_KEY = "instructions";
    protected static final String GENERATOR_KEY = "generator";
    protected static final String TIMESTAMP_KEY = "timestamp";

    private static final Gson GSON = new Gson();

    private String identifier;
    private String instructions;
    private String checkName;
    private String timestamp;
    private List<String> features;

    public GeoJsonCheckFlag(final String identifier, final String instructions,
            final String checkName, final String timestamp, final List<JsonObject> features)
    {
        this.identifier = identifier;
        this.instructions = instructions;
        this.checkName = checkName;
        this.timestamp = timestamp;
        this.setFeatures(features);
    }

    public String getCheckName()
    {
        return this.checkName;
    }

    public List<JsonObject> getFeatures()
    {
        return this.features.stream().map(string -> GSON.fromJson(string, JsonObject.class))
                .collect(Collectors.toList());
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    public String getInstructions()
    {
        return this.instructions;
    }

    public String getTimestamp()
    {
        return this.timestamp;
    }

    public void setCheckName(final String checkName)
    {
        this.checkName = checkName;
    }

    public void setFeatures(final List<JsonObject> features)
    {
        this.features = features.stream().map(JsonObject::toString).collect(Collectors.toList());
    }

    public void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    public void setInstructions(final String instructions)
    {
        this.instructions = instructions;
    }

    public void setTimestamp(final String timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Serializes the flag to be the same as the output of {@link CheckFlagEvent}'s
     * {@code .toString()}.
     *
     * @return a {@link String} serialization of the flag
     */
    public String toString()
    {
        final JsonObject featureCollection = new JsonObject();
        featureCollection.addProperty(TYPE, FEATURE_COLLECTION.getTypeString());
        featureCollection.add(FEATURES, GSON.toJsonTree(this.getFeatures()));
        featureCollection.add(PROPERTIES,
                GSON.toJsonTree(Maps.hashMap(IDENTIFIER_KEY, this.identifier, INSTRUCTIONS_KEY,
                        this.instructions, GENERATOR_KEY, this.checkName, TIMESTAMP_KEY,
                        this.timestamp)));

        return featureCollection.toString();
    }
}
