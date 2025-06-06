//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Main simulation controller
// */
//class CloudSimulation
//{
//    private final SimulationConfig config;
//    private final List<CloudResource> resources = new ArrayList<>();
//    private final List<Container> containers = new ArrayList<>();
//    private final MetricsCollector metrics = new MetricsCollector();
//
//    public CloudSimulation(final SimulationConfig config)
//    {
//        this.config = config;
//    }
//
//    public void setup()
//    {
//        System.out.println("Setting up simulation with " + config.numContainers +
//                " containers and " + config.numResources + " resources...");
//
//        // Create resources
//        for (int i = 0; i < config.numResources; i++)
//        {
//            String resourceId = "resource_" + i;
//            resources.add(new CloudResource(resourceId, config.maxConcurrentAccess));
//        }
//
//        // Create containers/ Clients
//        for (int i = 0; i < config.numContainers; i++)
//        {
//            containers.add(new Container(i, resources, config.enableSynchronization, config, metrics));
//        }
//    }
//
//    public void run()
//    {
//        System.out.println("Starting simulation " +
//                (config.enableSynchronization ? "with" : "without") +
//                " synchronization...");
//
//        // Start all containers
//        for (Container container : containers)
//        {
//            container.start();
//        }
//
//        // Run for the specified duration
//        try
//        {
//            Thread.sleep(config.simulationTimeSeconds * 1000L);
//        }
//        catch (InterruptedException e)
//        {
//            Thread.currentThread().interrupt();
//        }
//
//        // Stop all containers
//        for (Container container : containers)
//        {
//            container.stop();
//        }
//
//        // Wait a bit for threads to finish
//        try
//        {
//            Thread.sleep(1000);
//        }
//        catch (final InterruptedException e)
//        {
//            Thread.currentThread().interrupt();
//        }
//
//        // Print and save results
//        printResults();
//        metrics.saveToFile(config.metricsOutputFile, config);
//    }
//
//    public void printResults()
//    {
//        System.out.println("\n==== Simulation Results ====");
//        System.out.println("Configuration:");
//        System.out.println("- Containers: " + config.numContainers);
//        System.out.println("- Resources: " + config.numResources);
//        System.out.println("- Synchronization: " + (config.enableSynchronization ? "Enabled" : "Disabled"));
//        System.out.println("- Duration: " + config.simulationTimeSeconds + " seconds");
//
//        // Print resource statistics
//        System.out.println("\nResource Statistics:");
//        for (int i = 0; i < resources.size(); i++)
//        {
//            CloudResource resource = resources.get(i);
//            System.out.println("- Resource " + i + " (ID: " + resource.getId() + "):");
//            System.out.println("  - Total accesses: " + resource.getTotalAccesses());
//            System.out.println("  - Conflicts: " + resource.getConflictCount());
//        }
//
//        // Print overall metrics
//        metrics.printSummary();
//    }
//}