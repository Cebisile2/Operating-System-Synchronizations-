import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class CloudResource
{
    private final String resourceId;
    private final DistributedSemaphore semaphore;
    private static final int timeOutMs        = 1000;

    private final AtomicInteger currentUsers  = new AtomicInteger(0);
    private final AtomicInteger totalAccesses = new AtomicInteger(0);
    private final AtomicInteger conflictCount = new AtomicInteger(0);

    public CloudResource(final String id,
                         final int maxConcurrentAccess)
    {
        this.resourceId = id;
        this.semaphore  = new DistributedSemaphore(maxConcurrentAccess, "sem_" + id);
    }

    /**
     * Access the resource with synchronization
     */
    public void accessWithSync(int containerId, Random random,
                               int networkLatencyMeanMs, int networkLatencyStdDevMs,
                               int processingTimeMeanMs, int processingTimeStdDevMs,
                               MetricsCollector metrics, SimulationVisualizer visualizer) {
        System.out.println("Container " + containerId + " accessing resource " + resourceId + " WITH sync");

        long startTime = System.currentTimeMillis();

        // Try to acquire the semaphore with timeout
        boolean acquired = semaphore.acquire(random, networkLatencyMeanMs,
                networkLatencyStdDevMs, 5000);

        long acquireTime = System.currentTimeMillis();
        long acquireDuration = acquireTime - startTime;

        if (!acquired) {
            // Timeout occurred
            metrics.recordTimeout(containerId, resourceId);
            return;
        }

        // Update visualizer - resource acquired
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.accessResource(containerId, resourceId, true));
        }

        // Successfully acquired the semaphore
        int users = currentUsers.incrementAndGet();
        totalAccesses.incrementAndGet();

        if (visualizer != null) {
            System.out.println("Notifying visualizer: container " + containerId + " acquired " + resourceId);
            SwingUtilities.invokeLater(() ->
                    visualizer.accessResource(containerId, resourceId, true));
        }

        // Simulate processing time for using the resource
        int processingTime = Math.max(1, (int)(random.nextGaussian() *
                processingTimeStdDevMs +
                processingTimeMeanMs));
        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for potential conflicts (should never happen with proper synchronization)
        if (users > semaphore.getMaxValue()) {
            conflictCount.incrementAndGet();
            metrics.recordConflict(containerId, resourceId);

            // Update visualizer with conflict
            if (visualizer != null) {
                SwingUtilities.invokeLater(() ->
                        visualizer.recordConflict(containerId, resourceId));
            }
        }

        currentUsers.decrementAndGet();

        // Release the semaphore
        semaphore.release(random, networkLatencyMeanMs, networkLatencyStdDevMs);

        // Update visualizer - resource released
        if (visualizer != null) {
            System.out.println("Notifying visualizer: container " + containerId + " released " + resourceId);
            SwingUtilities.invokeLater(() ->
                    visualizer.releaseResource(containerId, resourceId));
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Record metrics
        metrics.recordAccess(containerId, resourceId, acquireDuration, processingTime, totalDuration);

        // Update operation count in visualizer
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.recordOperation());
        }
    }

    /**
     * Access the resource without synchronization (for comparison)
     */
    public void accessWithoutSync(int containerId, Random random,
                                  int networkLatencyMeanMs, int networkLatencyStdDevMs,
                                  int processingTimeMeanMs, int processingTimeStdDevMs,
                                  MetricsCollector metrics, SimulationVisualizer visualizer) {

        long startTime = System.currentTimeMillis();

        // Simulate network latency (but no semaphore acquisition)
        simulateNetworkLatency(random, networkLatencyMeanMs, networkLatencyStdDevMs);

        long acquireTime = System.currentTimeMillis();
        long acquireDuration = acquireTime - startTime;

        // Update visualizer - resource acquired
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.accessResource(containerId, resourceId, false));
        }

        // No synchronization, just access the resource
        int users = currentUsers.incrementAndGet();
        totalAccesses.incrementAndGet();

        // Simulate processing time
        int processingTime = Math.max(1, (int)(random.nextGaussian() *
                processingTimeStdDevMs +
                processingTimeMeanMs));
        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for conflicts (will happen without synchronization)
        boolean conflict = users > semaphore.getMaxValue();
        if (conflict) {
            conflictCount.incrementAndGet();
            metrics.recordConflict(containerId, resourceId);

            // Update visualizer with conflict
            if (visualizer != null) {
                SwingUtilities.invokeLater(() ->
                        visualizer.recordConflict(containerId, resourceId));
            }
        }

        currentUsers.decrementAndGet();

        // Simulate network latency for completion
        simulateNetworkLatency(random, networkLatencyMeanMs, networkLatencyStdDevMs);

        // Update visualizer - resource released
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.releaseResource(containerId, resourceId));
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Record metrics
        metrics.recordAccess(containerId, resourceId, acquireDuration, processingTime, totalDuration);

        // Update operation count in visualizer
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.recordOperation());
        }
    }

    private void simulateNetworkLatency(final Random random,
                                        final int meanMs,
                                        final int stdDevMs)
    {
        int latency = Math.max(1, (int)(random.nextGaussian() * stdDevMs + meanMs));
        try
        {
            Thread.sleep(latency);
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    // Getters for metrics
    public int getTotalAccesses()
    {
        return totalAccesses.get();
    }

    public int getConflictCount()
    {
        return conflictCount.get();
    }

    public int getCurrentUsers()
    {
        return currentUsers.get();
    }

    public String getId()
    {
        return resourceId;
    }
}