import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Random;

public class Container implements Runnable {
    private final int containerId;
    private final List<CloudResource> resources;
    private final boolean enableSync;
    private final Random random = new Random();
    private final MetricsCollector metrics;
    private final SimulationVisualizer visualizer;
    private volatile boolean running = false;

    // Normal distribution parameters
    private final int networkLatencyMeanMs;
    private final int networkLatencyStdDevMs;
    private final int processingTimeMeanMs;
    private final int processingTimeStdDevMs;
    private final int requestRateMeanMs;
    private final int requestRateStdDevMs;

    public Container(int id, List<CloudResource> resources, boolean enableSync,
                     int networkLatencyMeanMs, int networkLatencyStdDevMs,
                     int processingTimeMeanMs, int processingTimeStdDevMs,
                     int requestRateMeanMs, int requestRateStdDevMs,
                     MetricsCollector metrics, SimulationVisualizer visualizer) {
        this.containerId = id;
        this.resources = resources;
        this.enableSync = enableSync;
        this.networkLatencyMeanMs = networkLatencyMeanMs;
        this.networkLatencyStdDevMs = networkLatencyStdDevMs;
        this.processingTimeMeanMs = processingTimeMeanMs;
        this.processingTimeStdDevMs = processingTimeStdDevMs;
        this.requestRateMeanMs = requestRateMeanMs;
        this.requestRateStdDevMs = requestRateStdDevMs;
        this.metrics = metrics;
        this.visualizer = visualizer;

        // Seed with container ID for more reproducible results
        random.setSeed(System.currentTimeMillis() + id);
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            // Select a random resource to access
            int resourceIndex = random.nextInt(resources.size());
            CloudResource resource = resources.get(resourceIndex);

            // Access the resource with or without synchronization
            if (enableSync) {
                accessWithSync(resource);
            } else {
                accessWithoutSync(resource);
            }

            // Wait before next request
            int waitTime = Math.max(500, (int)(random.nextGaussian() *
                    requestRateStdDevMs +
                    requestRateMeanMs));
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void accessWithSync(CloudResource resource)
    {
        long startTime = System.currentTimeMillis();

        // Access with synchronization
        resource.accessWithSync(containerId, random,
                networkLatencyMeanMs, networkLatencyStdDevMs,
                processingTimeMeanMs, processingTimeStdDevMs,
                metrics, visualizer);

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Update visualizer with response time
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.updateResponseTime(responseTime));
        }
    }

    private void accessWithoutSync(CloudResource resource)
    {
        long startTime = System.currentTimeMillis();

        // Access without synchronization
        resource.accessWithoutSync(containerId, random,
                networkLatencyMeanMs, networkLatencyStdDevMs,
                processingTimeMeanMs, processingTimeStdDevMs,
                metrics, visualizer);

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Update visualizer with response time
        if (visualizer != null) {
            SwingUtilities.invokeLater(() ->
                    visualizer.updateResponseTime(responseTime));
        }
    }
}