package Semaphore;

import java.util.concurrent.CountDownLatch;

/**
 * Simulates a client connecting to a cloud database
 */
class DatabaseClient implements Runnable
{
    private final int clientId;
    private final SharedCloudResource resource;
    private final WorkLoadConfig config;
    private final boolean useSync;
    private final SimulationResults results;
    private final CountDownLatch startLatch;
    private final CountDownLatch finishLatch;

    public DatabaseClient(final int clientId,
                          final SharedCloudResource resource,
                          final WorkLoadConfig config,
                          final boolean useSync,
                          final SimulationResults results,
                          final CountDownLatch startLatch,
                          final CountDownLatch finishLatch)
    {
        this.clientId    = clientId;
        this.resource    = resource;
        this.config      = config;
        this.useSync     = useSync;
        this.results     = results;
        this.startLatch  = startLatch;
        this.finishLatch = finishLatch;
    }

    @Override
    public void run()
    {
        try
        {
            // Wait for all clients to be ready
            startLatch.await();

            for (int i = 0; i < config.operationsPerClient; i++)
            {
                // Measure operation time
                long startTime = System.currentTimeMillis();

                // Access the resource with or without synchronization
                boolean noConflict;
                if (useSync)
                {
                    noConflict = resource.accessWithSync(getOperationTime());
                }
                else
                {
                    noConflict = resource.accessWithoutSync(getOperationTime());
                }

                // Calculate response time
                long endTime      = System.currentTimeMillis();
                long responseTime = endTime - startTime;

                // Record metrics
                results.totalOperations.incrementAndGet();
                results.addResponseTime(responseTime);

                if (!noConflict)
                {
                    results.conflictCount.incrementAndGet();
                }

                // Wait between operations if delay is specified
                if (config.delayBetweenOperationsMs > 0)
                {
                    Thread.sleep(config.delayBetweenOperationsMs);
                }
                else if (config.delayBetweenOperationsMs < 0)
                {
                    // Random delay for mixed workload
                    Thread.sleep((long)(Math.random() * 100));
                }
            }
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            // Signal that this client has finished
            finishLatch.countDown();
        }
    }

    /**
     * Get the operation time (constant or variable based on client ID)
     */
    private long getOperationTime()
    {
        // Base operation time 20-50ms depending on client ID
        // This simulates different operation types
        return 20 + (clientId % 4) * 10;
    }
}