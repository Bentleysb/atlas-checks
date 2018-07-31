package org.openstreetmap.atlas.checks.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.tags.HighwayTag;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Wraps a {@link CheckFlag} for submission to the {@link EventService} for handling {@link Check}
 * results
 *
 * @author mkalender
 */
public final class CheckFlagEvent extends Event
{
    private static final GeoJsonBuilder GEOJSON_BUILDER = new GeoJsonBuilder();

    private final String checkName;
    private final CheckFlag flag;

    /**
     * Converts given {@link CheckFlag} to a {@link GeoJsonObject} FeatureCollection, with
     * additional key-value parameters
     *
     * @param flag
     *            {@link CheckFlag} to convert to {@link GeoJsonObject}
     * @param additionalProperties
     *            additional key-value parameters to be added in "properties" element of the
     *            top-level JSON object
     * @return {@link GeoJsonObject} created from {@link CheckFlag}
     */
    public static JsonObject flagToFeature(final CheckFlag flag,
            final Map<String, String> additionalProperties)
    {
        final JsonObject flagProperties = new JsonObject();
        flagProperties.addProperty("instructions", flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach(flagProperties::addProperty);

        final List<GeoJsonObject> flagFeatures = new ArrayList<>();
        // Get the geometry of each object based on its type
        for (final FlaggedObject object : flag.getFlaggedObjects())
        {
            final GeoJsonObject flagFeature;
            // Check if the object is a Location
            if (object.getProperties().get("ItemType") != null)
            {
                switch (object.getProperties().get("ItemType"))
                {
                    case "Node":
                    case "Point":
                    {
                        flagFeature = GEOJSON_BUILDER.create(object.getGeometry(),
                                GeoJsonBuilder.GeoJsonType.POINT);
                        break;
                    }
                    case "Edge":
                    case "Line":
                    {
                        flagFeature = GEOJSON_BUILDER.create(object.getGeometry(),
                                GeoJsonBuilder.GeoJsonType.LINESTRING);
                        break;
                    }
                    case "Area":
                    {
                        flagFeature = GEOJSON_BUILDER.create(object.getGeometry(),
                                GeoJsonBuilder.GeoJsonType.POLYGON);
                        break;
                    }
                    default:
                        flagFeature = null;
                }
            }
            else
            {
                // Handle Locations like points
                flagFeature = GEOJSON_BUILDER.create(object.getGeometry(),
                        GeoJsonBuilder.GeoJsonType.POINT);
            }
            if (flagFeature != null)
            {
                // Add the objects properties to its feature object and add the feature to the list
                flagFeatures.add(flagFeature.withNewProperties(object.getProperties()));
            }
        }

        // Create a FeatureCollection of the flag objects in their geojson feature form
        final JsonObject feature = GEOJSON_BUILDER.createFeatureCollection(flagFeatures)
                .jsonObject();
        // Add the flag info to the feature collection
        feature.addProperty("id", flag.getIdentifier());
        feature.add("properties", flagProperties);
        return feature;
    }

    /**
     * Converts given {@link CheckFlag} to {@link JsonObject} with additional key-value parameters
     *
     * @param flag
     *            {@link CheckFlag} to convert to {@link JsonObject}
     * @param additionalProperties
     *            additional key-value parameters to be added in "properties" element of the
     *            top-level JSON object
     * @return {@link JsonObject} created from {@link CheckFlag}
     */
    public static JsonObject flagToJson(final CheckFlag flag,
            final Map<String, String> additionalProperties)
    {
        final JsonObject flagJson = GEOJSON_BUILDER.create(flag.getLocationIterableProperties())
                .jsonObject();
        final JsonObject flagPropertiesJson = new JsonObject();
        flagPropertiesJson.addProperty("id", flag.getIdentifier());
        flagPropertiesJson.addProperty("instructions", flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach(flagPropertiesJson::addProperty);

        // Add properties to the previously generate geojson
        flagJson.add("properties", flagPropertiesJson);
        return flagJson;
    }

    /**
     * Extracts a decorator based on the collective features properties. Currently the only
     * decoration is the highest class highway tag withing all of the feature properties for flags
     * involving Edges.
     */
    private static Optional<String> featureDecorator(final JsonArray featureProperties)
    {
        HighwayTag highestHighwayTag = null;
        for (final JsonElement featureProperty : featureProperties)
        {
            final HighwayTag baslineHighwayTag = highestHighwayTag == null ? HighwayTag.NO
                    : highestHighwayTag;
            highestHighwayTag = Optional
                    .ofNullable(((JsonObject) featureProperty).getAsJsonPrimitive(HighwayTag.KEY))
                    .map(JsonPrimitive::getAsString).map(String::toUpperCase)
                    .map(HighwayTag::valueOf).filter(baslineHighwayTag::isLessImportantThan)
                    .orElse(highestHighwayTag);
        }
        return Optional.ofNullable(highestHighwayTag)
                .map(tag -> String.format("%s=%s", HighwayTag.KEY, tag.getTagValue()));
    }

    /**
     * Construct a {@link CheckFlagEvent}
     *
     * @param checkName
     *            name of the check that created this event
     * @param flag
     *            {@link CheckFlag} generated within this event
     */
    public CheckFlagEvent(final String checkName, final CheckFlag flag)
    {
        this.checkName = checkName;
        this.flag = flag;
    }

    /**
     * @return {@link CheckFlag} generated by the check
     */
    public CheckFlag getCheckFlag()
    {
        return this.flag;
    }

    /**
     * @return Name of the check that generated this event
     */
    public String getCheckName()
    {
        return this.checkName;
    }

    /**
     * @return GeoJson Feature representation
     */
    public JsonObject toGeoJsonFeature()
    {
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("name",
                this.getCheckFlag().getChallengeName().orElse(this.getCheckName()));
        contextualProperties.put("generator", "AtlasChecks");
        contextualProperties.put("timestamp", this.getTimestamp().toString());

        // Generate json for check flag with given contextual properties
        return flagToFeature(this.getCheckFlag(), contextualProperties);
    }

    /**
     * @return {@link JsonObject} form of the GeoJson FeatureCollection representation
     */
    public JsonObject toGeoJsonFeatureCollection()
    {
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", this.getCheckName());
        contextualProperties.put("timestamp", this.getTimestamp().toString());

        // Generate json for check flag with given contextual properties
        return flagToJson(this.getCheckFlag(), contextualProperties);
    }

    /**
     * @return {@link String} form of the GeoJson FeatureCollection representation
     */
    @Override
    public String toString()
    {
        return this.toGeoJsonFeatureCollection().toString();
    }
}
