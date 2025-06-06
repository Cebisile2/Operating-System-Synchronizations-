/**
 * Configuration parameters for the simulation
 */
public class SimulationConfig
{
    int numContainers             = 20;           // Number of containers/clients
    int numResources              = 5;             // Number of shared resources
    int simulationTimeSeconds     = 60;   // Total simulation time
    int maxConcurrentAccess       = 3;      // Max concurrent access per resource (semaphore value)
    boolean enableSynchronization = true; // Toggle synchronization on/off for comparison
    int networkLatencyMeanMs      = 15;    // Mean network latency in ms
    int networkLatencyStdDevMs    = 5;   // Standard deviation for network latency
    int processingTimeMeanMs      = 50;    // Mean processing time in ms
    int processingTimeStdDevMs    = 20;  // Standard deviation for processing time
    int requestRateMeanMs         = 200;      // Mean time between requests from a client
    int requestRateStdDevMs       = 100;    // Standard deviation for request rate
    boolean enableLogging         = true;     // Enable detailed logging
    String metricsOutputFile      = "sync_on_simulation_metrics.csv";
}
