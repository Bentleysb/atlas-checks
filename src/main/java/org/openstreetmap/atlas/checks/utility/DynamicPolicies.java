package org.openstreetmap.atlas.checks.utility;

import org.apache.spark.broadcast.Broadcast;
import org.openstreetmap.atlas.checks.distributed.ShardedCheckFlagsTask;
import org.openstreetmap.atlas.generator.tools.caching.HadoopAtlasFileCache;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum DynamicPolicies {
    EVERYTHING_TEN_KILOMETER_EXPANSION;


    private static final Distance TEN_KILOMETERS = Distance.kilometers(10);

    private static Function<Shard, Optional<Atlas>> getFetcher(final String atlasDirectory, final Map<String, String> configurationMap, final AtlasResourceLoader loader, final String country) {
        final HadoopAtlasFileCache cache = new HadoopAtlasFileCache(atlasDirectory, configurationMap);
        return (Function<Shard, Optional<Atlas>> & Serializable) shard ->
        {
            return cache.get(country, shard).map(loader::load);
        };
    }

    public static DynamicAtlasPolicy everythingTenKilometerExpansion(final String atlasDirectory, final ShardedCheckFlagsTask task, final Map<String, String> configurationMap, final Broadcast<Sharding> sharding){
        return new DynamicAtlasPolicy(getFetcher(atlasDirectory, configurationMap, new AtlasResourceLoader(), task.getCountry()), sharding.getValue(), new HashSet<>(task.getShardGroup()),
                Rectangle.forLocated(task.getShardGroup()).bounds()
                        .expand(TEN_KILOMETERS)).withDeferredLoading(true)
                .withAggressivelyExploreRelations(true)
                .withExtendIndefinitely(false);
    }
}
