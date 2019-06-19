package org.openstreetmap.atlas.checks.event;

import com.codahale.metrics.Metric;
import com.google.common.eventbus.Subscribe;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteClient;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.event.Processor;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * A class that will handle the MapRouletteClient for every check that it is constructed with and will guarantee an attemped upload
 *
 * @author jklamer
 */
public class MapRouletteClientProcessor implements Processor<CheckFlagEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteClientProcessor.class);
    private static final int MINIMUM_DURATION_SECONDS = 5;
    private static final int MINIMUM_DURATION_RATIO = 10;

    /**
     * Calculates max {@link Duration} timeout for given batch size
     *
     * @param batchSize
     *            batch size to calculate timeout for
     * @return {@link Duration} timeout for given batch size
     */
    private static Duration maxDurationForBatch(final int batchSize)
    {
        return Duration
                .seconds(Math.max(MINIMUM_DURATION_SECONDS, batchSize / MINIMUM_DURATION_RATIO));
    }

    private final HashMap<String, MapRouletteClient> checkToClient = new HashMap<>();
    private final HashMap<String, Challenge> checkToChallenge = new HashMap<>();

    /**
     * Processor to add tasks to clients and send at the end
     * @param configuration
     * @param checks
     */
    public MapRouletteClientProcessor(final MapRouletteConfiguration configuration, final Iterable<BaseCheck> checks)
    {
        for (final BaseCheck check : checks)
        {
            this.checkToClient.put(check.getCheckName(), MapRouletteClient.instance(configuration));
            this.checkToChallenge.put(check.getCheckName(), check.getChallenge());
        }
    }

    @Subscribe
    @Override
    public void process(final CheckFlagEvent event)
    {
        try
        {
            this.checkToClient.get(event.getCheckName()).addTask(this.checkToChallenge.get(event.getCheckName()), event.getCheckFlag().getMapRouletteTask());
        }
        catch (final Exception e)
        {
            logger.warn("Failed to create a MapRoulette task for [{}, {}]. Exception: {}.",
                    event.getCheckName(), event.getCheckName(), e);
        }
    }

    @Subscribe
    public void process(final MetricEvent metricEvent)
    {
        final MapRouletteClient client = this.checkToClient.remove(metricEvent.getName());
        if (client != null)
        {
            try (Pool uploadPool = new Pool(1,
                    String.format("MR upload pool for %s (%s)", metricEvent.getName()),
                    maxDurationForBatch(client.getCurrentBatchSize())))
            {
                uploadPool.queue(() -> client.uploadTasks());
            }
            catch (final Exception e)
            {
                logger.error("Failed to upload tasks to MapRoulette client {}.", client, e);
            }
        }
        else
        {
            // setting as trace otherwise will just through
            logger.trace(
                    "Ignoring upload to MapRoulette. Client was never initialized correctly. See beginning of log for more details.");
        }

    }

    @Subscribe
    @Override
    public void process(final ShutdownEvent event)
    {
        this.uploadAllRemainingTasks();
    }

    /**
     * If metric events are dropped or forgotten, attempt upload of everything left
     */
    protected void uploadAllRemainingTasks()
    {
        if(!this.checkToClient.isEmpty())
        {
            try (Pool uploadPool = new Pool(1,
                    String.format("MR upload pool for %s (%s)", metricEvent.getName()),
                    maxDurationForBatch()))
            {
                uploadPool.queue(() -> client.uploadTasks());
            }
            catch (final Exception e)
            {
                logger.error("Failed to upload tasks to MapRoulette clients {}.", this.checkToClient.values(), e);
            }
        }
    }
}
