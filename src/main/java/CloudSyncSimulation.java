import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class CloudSyncSimulation
{
    // Add a visualizer field
    private SimulationVisualizer visualizer;
    private SimulationConfig config;
    private List<CloudResource> resources;
    private List<Container> containers;
    private MetricsCollector metrics;

    public CloudSyncSimulation(SimulationConfig config) {
        this.config = config;

        // Create visualizer
        SwingUtilities.invokeLater(() -> {
            visualizer = new SimulationVisualizer();
            visualizer.setSyncEnabled(config.enableSynchronization);
            visualizer.setStatus("Simulation initialized");
            visualizer.runUITest();
        });
    }

    public void setup() {
        System.out.println("Setting up simulation with " + config.numContainers
                + " containers and " + config.numResources + " resources...");

        // Create metrics collector
        metrics = new MetricsCollector();

        // Create resources
        resources = new ArrayList<>();
        for (int i = 0; i < config.numResources; i++) {
            String resourceId = "resource_" + i;
            CloudResource resource = new CloudResource(resourceId, config.maxConcurrentAccess);
            resources.add(resource);

            // Register with visualizer
            final String resId = resourceId;
            SwingUtilities.invokeLater(() -> {
                if (visualizer != null) {
                    visualizer.registerResource(resId);
                }
            });
        }

        // Create containers
        containers = new ArrayList<>();
        for (int i = 0; i < config.numContainers; i++) {
            Container container = new Container(
                    i, resources, config.enableSynchronization,
                    config.networkLatencyMeanMs, config.networkLatencyStdDevMs,
                    config.processingTimeMeanMs, config.processingTimeStdDevMs,
                    config.requestRateMeanMs, config.requestRateStdDevMs,
                    metrics, visualizer
            );
            containers.add(container);
        }
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            if (visualizer != null) {
                visualizer.setStatus("Starting simulation " +
                        (config.enableSynchronization ? "with" : "without") +
                        " synchronization...");
            }
        });

        // Start all containers
        for (Container container : containers) {
            container.start();
        }

        // Run for the specified duration
        try {
            Thread.sleep(config.simulationTimeSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stop all containers
        for (Container container : containers) {
            container.stop();
        }

        SwingUtilities.invokeLater(() -> {
            if (visualizer != null) {
                visualizer.setStatus("Simulation completed");
            }
        });

        // Print and save results
        printResults();
        metrics.saveToFile(config.metricsOutputFile, config);
    }

    private void printResults() {
        System.out.println("\n==== Simulation Results ====");
        System.out.println("Configuration:");
        System.out.println("- Containers: " + config.numContainers);
        System.out.println("- Resources: " + config.numResources);
        System.out.println("- Synchronization: " + (config.enableSynchronization ? "Enabled" : "Disabled"));
        System.out.println("- Duration: " + config.simulationTimeSeconds + " seconds");

        // Print resource statistics
        System.out.println("\nResource Statistics:");
        for (int i = 0; i < resources.size(); i++) {
            CloudResource resource = resources.get(i);
            System.out.println("- Resource " + i + " (ID: " + resource.getId() + "):");
            System.out.println("  - Total accesses: " + resource.getTotalAccesses());
            System.out.println("  - Conflicts: " + resource.getConflictCount());
        }

        // Print overall metrics
        metrics.printSummary();
    }

    // Rest of your class remains the same
}