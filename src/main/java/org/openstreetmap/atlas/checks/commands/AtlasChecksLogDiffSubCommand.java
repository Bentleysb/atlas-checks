package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.openstreetmap.atlas.streaming.resource.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Takes 2 sets of atlas-checks log files and reports the number of additions, subtractions, and
 * changed flags from source to target. Optionally, the reported items can be written to new log
 * files.
 *
 * @author bbreithaupt
 */
public class AtlasChecksLogDiffSubCommand extends AbstractJsonFlagDiffSubCommand
{
    public AtlasChecksLogDiffSubCommand()
    {
        super("log-diff",
                "Takes 2 sets of atlas-checks log flag files and reports the number of additions, subtractions, and changed flags from source to target.",
                "log");
    }

    @Override
    protected void mapFeatures(final File file, final HashMap map)
    {
        try
        {
            final BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
            String line;
            // Read each line (flag) from the log file
            while ((line = reader.readLine()) != null)
            {
                // Parse the json
                final JsonObject source = getGson().fromJson(line, JsonObject.class);
                // Get the check name
                final String checkName = source.get("properties").getAsJsonObject().get("generator")
                        .getAsString();
                // Add the check name as a key
                if (!map.containsKey(checkName))
                {
                    map.put(checkName, new HashMap<>());
                }
                // Add the geoJSON as a value
                ((HashMap<String, HashMap>) map).get(checkName).put(
                        source.get("properties").getAsJsonObject().get("id").getAsString(), source);
            }
        }
        catch (final IOException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    protected ArrayList<HashSet<JsonObject>> getMissingAndChanged(final HashMap source,
            final HashMap target, final boolean onlyMissing)
    {
        final HashSet<JsonObject> missing = new HashSet<>();
        final HashSet<JsonObject> changed = new HashSet<>();
        source.forEach((check, flag) ->
        {
            ((HashMap<String, JsonObject>) flag).forEach((identifier, featureCollection) ->
            {
                // Get missing
                if (!target.containsKey(check)
                        || !((HashMap<String, HashMap>) target).get(check).containsKey(identifier))
                {
                    missing.add(featureCollection);
                }
                // If not missing, check for Atlas id changes
                else if (!onlyMissing
                        && !identicalFeatureIds(featureCollection.get("features").getAsJsonArray(),
                                ((HashMap<String, HashMap<String, JsonObject>>) target).get(check)
                                        .get(identifier).get("features").getAsJsonArray()))
                {
                    changed.add(featureCollection);
                }
            });
        });
        final ArrayList<HashSet<JsonObject>> missingAndChanged = new ArrayList<>();
        missingAndChanged.add(missing);
        missingAndChanged.add(changed);
        return missingAndChanged;
    }

    @Override
    protected boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        // The array must be the same size to match
        if (sourceArray.size() != targetArray.size())
        {
            return false;
        }
        // Gather all the source ids
        sourceArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get("properties").getAsJsonObject().has("ItemId"))
            {
                sourceIds.add(object.getAsJsonObject().get("properties").getAsJsonObject()
                        .get("ItemId").getAsString());
            }
        });
        // Gather all the target ids
        targetArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get("properties").getAsJsonObject().has("ItemId"))
            {
                targetIds.add(object.getAsJsonObject().get("properties").getAsJsonObject()
                        .get("ItemId").getAsString());
            }
        });
        // Compare the two id lists
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }

    @Override
    protected int getSourceSize()
    {
        int sourceSize = 0;
        for (final String check : ((HashMap<String, HashMap>) getSource()).keySet())
        {
            sourceSize += ((HashMap<String, HashMap>) getSource()).get(check).size();
        }
        return sourceSize;
    }
}
