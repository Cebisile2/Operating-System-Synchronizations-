package Semaphore;

/**
 * Configuration for a workload simulation
 */
class WorkLoadConfig
{
    final String testName;
    final int numClients;
    final int operationsPerClient;
    final int delayBetweenOperationsMs;
    final int semaphorePermits;

    public WorkLoadConfig(final String testName,
                          final int numClients,
                          final int operationsPerClient,
                          final int delayBetweenOperationsMs,
                          final int semaphorePermits)
    {
        this.testName                 = testName;
        this.numClients               = numClients;
        this.operationsPerClient      = operationsPerClient;
        this.delayBetweenOperationsMs = delayBetweenOperationsMs;
        this.semaphorePermits         = semaphorePermits;
    }
}