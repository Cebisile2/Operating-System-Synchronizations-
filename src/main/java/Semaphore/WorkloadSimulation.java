package Semaphore;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This class simulates different workload scenarios to evaluate the effectiveness
 * of semaphores in preventing race conditions in a cloud environment.
 */
public class WorkloadSimulation
{

    public static void main(String[] args)
    {
        System.out.println("Starting Cloud Database Synchronization Workload Simulation");
        System.out.println("===========================================================");

        // Run different workload scenarios
        runLowConcurrencyTest();
        runMediumConcurrencyTest();
        runHighConcurrencyTest();
        runBurstTest();
        runMixedWorkloadTest();

        System.out.println("\nAll simulation tests completed. Results have been saved to CSV files.");
    }

    /**
     * Simulates a scenario with low concurrency (few concurrent requests)
     */
    private static void runLowConcurrencyTest()
    {
        System.out.println("\nRunning Low Concurrency Test (10 concurrent clients)");
        WorkLoadConfig config = new WorkLoadConfig(
                "low_concurrency_test",
                10,    // 10 clients
                5,     // 5 operations per client
                100,   // 100ms between operations
                3      // semaphore permits (max 3 concurrent accesses)
        );

        runSimulation(config);
    }

    /**
     * Simulates a scenario with medium concurrency
     */
    private static void runMediumConcurrencyTest()
    {
        System.out.println("\nRunning Medium Concurrency Test (25 concurrent clients)");
        WorkLoadConfig config = new WorkLoadConfig(
                "medium_concurrency_test",
                25,    // 25 clients
                10,    // 10 operations per client
                50,    // 50ms between operations
                3      // semaphore permits
        );

        runSimulation(config);
    }

    /**
     * Simulates a scenario with high concurrency (many concurrent requests)
     */
    private static void runHighConcurrencyTest()
    {
        System.out.println("\nRunning High Concurrency Test (50 concurrent clients)");
        WorkLoadConfig config = new WorkLoadConfig(
                "high_concurrency_test",
                50,    // 50 clients
                20,    // 20 operations per client
                25,    // 25ms between operations
                3      // semaphore permits
        );

        runSimulation(config);
    }

    /**
     * Simulates a burst scenario (sudden spike in requests)
     */
    private static void runBurstTest()
    {
        System.out.println("\nRunning Burst Test (40 concurrent clients with 0ms delay)");
        WorkLoadConfig config = new WorkLoadConfig(
                "burst_test",
                40,    // 40 clients
                5,     // 5 operations per client
                0,     // 0ms between operations (burst)
                3      // semaphore permits
        );

        runSimulation(config);
    }

    /**
     * Simulates a mixed workload with varying client behavior
     */
    private static void runMixedWorkloadTest()
    {
        System.out.println("\nRunning Mixed Workload Test (30 clients with mixed behavior)");
        WorkLoadConfig config = new WorkLoadConfig(
                "mixed_workload_test",
                30,    // 30 clients
                15,    // 15 operations per client
                -1,    // Mixed delays (-1 indicates random delays)
                3      // semaphore permits
        );

        runSimulation(config);
    }

    /**
     * Main simulation runner
     */
    private static void runSimulation(final WorkLoadConfig config)
    {
        // Create results objects to track metrics
        SimulationResults withSyncResults = new SimulationResults();
        SimulationResults withoutSyncResults = new SimulationResults();

        // Run with synchronization
        System.out.println("  - Running with semaphore synchronization...");
        runWorkload(config, true, withSyncResults);

        // Run without synchronization
        System.out.println("  - Running without synchronization...");
        runWorkload(config, false, withoutSyncResults);

        // Display summary
        System.out.println("  - Results Summary:");
        System.out.println("    * With Synchronization:");
        System.out.println("      - Operations: " + withSyncResults.totalOperations);
        System.out.println("      - Conflicts: " + withSyncResults.conflictCount +
                " (" + (withSyncResults.conflictCount.get() * 100.0 / withSyncResults.totalOperations.get()) + "%)");
        System.out.println("      - Average Response Time: " + withSyncResults.getAverageResponseTime() + "ms");

        System.out.println("    * Without Synchronization:");
        System.out.println("      - Operations: " + withoutSyncResults.totalOperations);
        System.out.println("      - Conflicts: " + withoutSyncResults.conflictCount +
                " (" + (withoutSyncResults.conflictCount.get() * 100.0 / withoutSyncResults.totalOperations.get()) + "%)");
        System.out.println("      - Average Response Time: " + withoutSyncResults.getAverageResponseTime() + "ms");

        // Save results to CSV
        saveResultsToCSV(config.testName, withSyncResults, withoutSyncResults);
    }

    /**
     * Run a specific workload scenario
     */
    private static void runWorkload(final WorkLoadConfig config,
                                    final boolean useSync,
                                    final SimulationResults results)
    {
        // Create shared resource and semaphore
        SharedCloudResource resource = new SharedCloudResource(config.semaphorePermits);

        // Create clients
        List<DatabaseClient> clients = new ArrayList<>();
        CountDownLatch startLatch    = new CountDownLatch(1);
        CountDownLatch finishLatch   = new CountDownLatch(config.numClients);

        for (int i = 0; i < config.numClients; i++)
        {
            DatabaseClient client = new DatabaseClient(
                    i, resource, config, useSync, results, startLatch, finishLatch);
            clients.add(client);
            new Thread(client).start();
        }

        // Start all clients simultaneously
        long startTime = System.currentTimeMillis();
        startLatch.countDown();

        try
        {
            // Wait for all clients to complete
            finishLatch.await();
            long endTime = System.currentTimeMillis();
            results.totalDurationMs = endTime - startTime;

        }
        catch (final InterruptedException e)
        {
            System.err.println("Simulation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Save results to CSV file for further analysis
     */
    private static void saveResultsToCSV(final String testName,
                                         final SimulationResults withSyncResults,
                                         final SimulationResults withoutSyncResults)
    {
        try (FileWriter writer = new FileWriter(testName + "_results.csv"))
        {
            // Write header
            writer.write("Metric,With Synchronization,Without Synchronization\n");

            // Write metrics
            writer.write("Total Operations," + withSyncResults.totalOperations +
                    "," + withoutSyncResults.totalOperations + "\n");
            writer.write("Conflict Count," + withSyncResults.conflictCount +
                    "," + withoutSyncResults.conflictCount + "\n");
            writer.write("Conflict Percentage," +
                    (withSyncResults.conflictCount.get() * 100.0 / withSyncResults.totalOperations.get()) +
                    "," +
                    (withoutSyncResults.conflictCount.get() * 100.0 / withoutSyncResults.totalOperations.get()) +
                    "\n");
            writer.write("Average Response Time (ms)," + withSyncResults.getAverageResponseTime() +
                    "," + withoutSyncResults.getAverageResponseTime() + "\n");
            writer.write("Total Duration (ms)," + withSyncResults.totalDurationMs +
                    "," + withoutSyncResults.totalDurationMs + "\n");
            writer.write("Throughput (ops/sec)," +
                    (withSyncResults.totalOperations.get() * 1000.0 / withSyncResults.totalDurationMs) +
                    "," +
                    (withoutSyncResults.totalOperations.get() * 1000.0 / withoutSyncResults.totalDurationMs) +
                    "\n");

            System.out.println("  - Results saved to " + testName + "_results.csv");

        }
        catch (final IOException e)
        {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }
}
