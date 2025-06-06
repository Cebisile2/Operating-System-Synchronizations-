import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics collection for performance analysis
 */
class MetricsCollector
{
    private final Object lock;
    private final List<AccessLogEntry> accessLogs;
    private final List<ConflictLogEntry> conflictLogs;
    private final List<ConflictLogEntry> timeoutLogs;

    // Performance metrics
    private final AtomicInteger totalRequests      = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger conflictCount      = new AtomicInteger(0);
    private final AtomicInteger timeoutCount       = new AtomicInteger(0);

    // Time tracking
    private final Instant startTime;

    public MetricsCollector()
    {
        accessLogs   = new ArrayList<>();
        conflictLogs = new ArrayList<>();
        timeoutLogs  = new ArrayList<>();
        lock         = new Object();
        startTime    = Instant.now();
    }

    public void recordAccess(final int containerId,
                             final String resourceId,
                             final long acquireTime,
                             final int processingTime,
                             final long totalTime)
    {
        synchronized (lock)
        {
            accessLogs.add(new AccessLogEntry(
                    containerId, resourceId, acquireTime, processingTime, totalTime));
        }
        totalRequests.incrementAndGet();
        successfulRequests.incrementAndGet();
    }

    public void recordConflict(final int containerId,
                               final String resourceId)
    {
        synchronized (lock)
        {
            conflictLogs.add(new ConflictLogEntry(containerId, resourceId));
        }
        conflictCount.incrementAndGet();
    }

    public void recordTimeout(final int containerId,
                              final String resourceId)
    {
        synchronized (lock)
        {
            timeoutLogs.add(new ConflictLogEntry(containerId, resourceId));
        }
        totalRequests.incrementAndGet();
        timeoutCount.incrementAndGet();
    }

    /**
     * Save metrics to CSV file
     */
    public void saveToFile(final String filename,
                           final SimulationConfig config)
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename)))
        {
            // Write simulation configuration
            writer.println("# Simulation Configuration");
            writer.println("NumContainers," + config.numContainers);
            writer.println("NumResources," + config.numResources);
            writer.println("SimulationTime," + config.simulationTimeSeconds);
            writer.println("MaxConcurrentAccess," + config.maxConcurrentAccess);
            writer.println("SynchronizationEnabled," + config.enableSynchronization);
            writer.println("NetworkLatencyMean," + config.networkLatencyMeanMs);
            writer.println("ProcessingTimeMean," + config.processingTimeMeanMs);
            writer.println();

            // Write summary metrics
            writer.println("# Summary Metrics");
            writer.println("TotalRequests," + totalRequests.get());
            writer.println("SuccessfulRequests," + successfulRequests.get());
            writer.println("Conflicts," + conflictCount.get());
            writer.println("Timeouts," + timeoutCount.get());

            Instant endTime = Instant.now();
            long duration = Duration.between(startTime, endTime).getSeconds();

            writer.println("TotalDuration," + duration);
            writer.println("ThroughputPerSecond," + ((float)successfulRequests.get() / Math.max(1, duration)));

            // Calculate average times
            double avgAcquireTime = 0;
            double avgProcessingTime = 0;
            double avgTotalTime = 0;

            synchronized (lock)
            {
                for (final AccessLogEntry log : accessLogs)
                {
                    avgAcquireTime += log.acquireTime;
                    avgProcessingTime += log.processingTime;
                    avgTotalTime += log.totalTime;
                }

                if (!accessLogs.isEmpty())
                {
                    avgAcquireTime /= accessLogs.size();
                    avgProcessingTime /= accessLogs.size();
                    avgTotalTime /= accessLogs.size();
                }
            }

            writer.println("AvgAcquireTimeMs," + avgAcquireTime);
            writer.println("AvgProcessingTimeMs," + avgProcessingTime);
            writer.println("AvgTotalTimeMs," + avgTotalTime);
            writer.println();

            // Write detailed access logs
            writer.println("# Access Logs");
            writer.println("ContainerId,ResourceId,AcquireTimeMs,ProcessingTimeMs,TotalTimeMs");

            synchronized (lock)
            {
                for (final AccessLogEntry log : accessLogs)
                {
                    writer.println(log.containerId + "," + log.resourceId + "," +
                            log.acquireTime + "," + log.processingTime + "," +
                            log.totalTime);
                }
            }

            writer.println();

            // Write conflict logs
            writer.println("# Conflict Logs");
            writer.println("ContainerId,ResourceId");

            synchronized (lock)
            {
                for (final ConflictLogEntry log : conflictLogs)
                {
                    writer.println(log.containerId + "," + log.resourceId);
                }
            }

            writer.println();

            // Write timeout logs
            writer.println("# Timeout Logs");
            writer.println("ContainerId,ResourceId");

            synchronized (lock)
            {
                for (final ConflictLogEntry log : timeoutLogs)
                {
                    writer.println(log.containerId + "," + log.resourceId);
                }
            }

        }
        catch (final IOException e)
        {
            System.err.println("Error saving metrics to file: " + e.getMessage());
        }
    }

    /**
     * Print summary to console
     */
    public void printSummary()
    {
        System.out.println("\n==== Simulation Summary ====");
        System.out.println("Total requests: " + totalRequests.get());
        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Conflicts: " + conflictCount.get());
        System.out.println("Timeouts: " + timeoutCount.get());

        Instant endTime = Instant.now();
        long duration   = Duration.between(startTime, endTime).getSeconds();

        System.out.println("Total duration: " + duration + " seconds");
        System.out.println("Throughput: " + ((float)successfulRequests.get() / Math.max(1, duration)) + " requests/second");

        // Calculate average times
        double avgAcquireTime = 0;
        double avgProcessingTime = 0;
        double avgTotalTime = 0;

        synchronized (lock)
        {
            for (final AccessLogEntry log : accessLogs)
            {
                avgAcquireTime += log.acquireTime;
                avgProcessingTime += log.processingTime;
                avgTotalTime += log.totalTime;
            }

            if (!accessLogs.isEmpty())
            {
                avgAcquireTime /= accessLogs.size();
                avgProcessingTime /= accessLogs.size();
                avgTotalTime /= accessLogs.size();
            }
        }

        System.out.println("Average acquire time: " + avgAcquireTime + " ms");
        System.out.println("Average processing time: " + avgProcessingTime + " ms");
        System.out.println("Average total time: " + avgTotalTime + " ms");
    }
}
