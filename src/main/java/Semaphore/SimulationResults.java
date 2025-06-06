package Semaphore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Results container for simulation metrics
 */
class SimulationResults {
    final AtomicInteger totalOperations = new AtomicInteger(0);
    final AtomicInteger conflictCount   = new AtomicInteger(0);
    final List<Long> responseTimes      = new ArrayList<>();
    long totalDurationMs                = 0;

    // Thread-safe addition of response time
    public synchronized void addResponseTime(long time)
    {
        responseTimes.add(time);
    }

    // Calculate average response time
    public double getAverageResponseTime()
    {
        synchronized (this)
        {
            if (responseTimes.isEmpty())
            {
                return 0;
            }

            long sum = 0;
            for (Long time : responseTimes)
            {
                sum += time;
            }
            return (double) sum / responseTimes.size();
        }
    }
}