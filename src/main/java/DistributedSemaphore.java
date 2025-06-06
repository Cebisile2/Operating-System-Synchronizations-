import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom implementation of distributed semaphore.
 * In a real distributed system, this would interact with a central coordination service.
 */
class DistributedSemaphore
{
    private static final Lock lock           = new ReentrantLock();
    private static final Condition condition = lock.newCondition();

    private int count;
    private final int maxCount;
    private final String name;

    /**
     * P operation (semWait)
     * @return true if acquired, false on timeout
     */
    public boolean acquire(final Random random,
                           final int networkLatencyMeanMs,
                           final int networkLatencyStdDevMs,
                           final long timeoutMs)
    {
        // Simulate network latency for an attempt lock acquisition
        simulateNetworkLatency(random, networkLatencyMeanMs, networkLatencyStdDevMs);

        lock.lock(); // This ensures that only one thread can modify the semaphore's state at a time
        try
        {
            if (timeoutMs < 0)
            {
                // Wait indefinitely until a resource becomes available.
                while (count <= 0)
                {
                    try
                    {
                        condition.await();  // Releases the lock since there are currently no resources available.
                    }
                    catch (final InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                count--;
                return true;
            }
            else
            {
                // Wait with timeout
                long nanos = TimeUnit.MILLISECONDS.toNanos(timeoutMs);
                while (count <= 0)
                {
                    if (nanos <= 0)
                        return false;
                    try
                    {
                        nanos = condition.awaitNanos(nanos);
                    }
                    catch (final InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                count--;
                return true;
            }
        } finally
        {
            lock.unlock();
        }
    }

    /**
     * V operation (signal/release)
     */
    public void release(final Random random,
                        final int networkLatencyMeanMs,
                        final int networkLatencyStdDevMs)
    {
        // Simulate network latency for distributed lock release
        simulateNetworkLatency(random, networkLatencyMeanMs, networkLatencyStdDevMs);

        lock.lock();
        try
        {
            count++;
            condition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Get the current semaphore value (for monitoring)
     */
    public int getValue()
    {
        lock.lock();
        try
        {
            return count;
        } finally
        {
            lock.unlock();
        }
    }

    /**
     * Get max semaphore value
     */
    public int getMaxValue()
    {
        return maxCount;
    }

    /**
     * Get semaphore name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Simulates network latency that would occur in a distributed system
     */
    private void simulateNetworkLatency(final Random random,
                                        final int meanMs,
                                        final int stdDevMs)
    {
        final int latency;

        // Generate latency with normal distribution
        latency = Math.max(1, (int)(random.nextGaussian() * stdDevMs + meanMs));
        try
        {
            Thread.sleep(latency);
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    public DistributedSemaphore(final int count,
                                final String name)
    {
        this.count    = count;  // The number of available permits or resources that can be acquired.
        this.maxCount = count;  // Stores the initial number of resources that can be acquired.
        this.name     = name;
    }
}
