package Semaphore;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates a cloud resource (e.g., database) that can be accessed by multiple clients
 */
class SharedCloudResource
{
    private final int maxConcurrentUsers;
    private final AtomicInteger currentUsers = new AtomicInteger(0);
    private final java.util.concurrent.Semaphore semaphore;

    public SharedCloudResource(final int maxConcurrentUsers)
    {
        this.maxConcurrentUsers = maxConcurrentUsers;
        this.semaphore          = new java.util.concurrent.Semaphore(maxConcurrentUsers, true);
    }

    /**
     * Access the resource with synchronization
     * @return true if no conflict occurred, false if conflict detected
     */
    public boolean accessWithSync(final long operationTimeMs)
    {
        try
        {
            // Acquire semaphore (blocks until permit available)
            semaphore.acquire();

            try
            {
                // Increment active users count
                int users = currentUsers.incrementAndGet();

                // Simulate database operation
                Thread.sleep(operationTimeMs);

                // Check if we exceeded max concurrent users (should never happen with semaphore)
                boolean conflict = users > maxConcurrentUsers;

                return !conflict;
            }
            finally
            {
                // Always decrement users and release semaphore
                currentUsers.decrementAndGet();
                semaphore.release();
            }
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Access the resource without synchronization
     * @return true if no conflict occurred, false if conflict detected
     */
    public boolean accessWithoutSync(final long operationTimeMs)
    {
        try
        {
            // Increment active users count (no synchronization)
            int users = currentUsers.incrementAndGet();

            // Simulate database operation
            Thread.sleep(operationTimeMs);

            // Check if we exceeded max concurrent users (will happen without semaphore)
            boolean conflict = users > maxConcurrentUsers;

            // Decrement users count
            currentUsers.decrementAndGet();

            return !conflict;
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
