import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationVisualizer extends JFrame
{
    private final int RESOURCE_COUNT = 5;

    private JPanel mainPanel;
    private Map<String, ResourcePanel> resourcePanels = new HashMap<>();
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JCheckBox syncEnabledCheckbox;

    private AtomicInteger conflictCount = new AtomicInteger(0);
    private AtomicInteger operationCount = new AtomicInteger(0);
    private AtomicInteger throughput = new AtomicInteger(0);
    private AtomicInteger avgResponseTime = new AtomicInteger(0);
    private long startTime;

    public SimulationVisualizer() {
        setTitle("Cloud Synchronization Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout());
        startPeriodicUpdates();

        // Top control panel
        JPanel controlPanel = new JPanel();
        syncEnabledCheckbox = new JCheckBox("Synchronization Enabled");
        syncEnabledCheckbox.setEnabled(false); // Just for display

        controlPanel.add(syncEnabledCheckbox);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Simulation initializing...");
        statsLabel = new JLabel("Operations: 0 | Conflicts: 0 | Throughput: 0 ops/sec | Avg Response: 0ms");
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(statsLabel, BorderLayout.SOUTH);

        // Resources panel
        JPanel resourcesPanel = new JPanel(new GridLayout(RESOURCE_COUNT, 1, 10, 10));

        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(resourcesPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Set up the frame
        setContentPane(mainPanel);

        // Start timing
        startTime = System.currentTimeMillis();

        // Start UI updater
        new Timer(100, e -> repaint()).start();

        setVisible(true);
    }

    public void runUITest() {
        new Thread(() -> {
            try {
                // Wait for UI to initialize
                Thread.sleep(1000);

                System.out.println("Starting UI test...");

                // Add containers to resource 0
                accessResource(1, "resource_0", true);
                Thread.sleep(500);
                accessResource(2, "resource_0", true);
                Thread.sleep(500);
                accessResource(3, "resource_0", true);

                // Add containers to resource 1
                accessResource(4, "resource_1", true);
                Thread.sleep(500);
                accessResource(5, "resource_1", true);

                // Create a conflict on resource 2
                accessResource(6, "resource_2", false);
                Thread.sleep(500);
                accessResource(7, "resource_2", false);
                Thread.sleep(500);
                accessResource(8, "resource_2", false);
                Thread.sleep(500);
                accessResource(9, "resource_2", false); // This should create a conflict

                // Wait a bit
                Thread.sleep(2000);

                // Release some containers
                releaseResource(1, "resource_0");
                releaseResource(4, "resource_1");
                releaseResource(7, "resource_2");

                System.out.println("UI test completed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void registerResource(String resourceId) {
        // Create panel for this resource if it doesn't exist
        JPanel resourcesPanel = (JPanel)mainPanel.getComponent(1);
        ResourcePanel panel = new ResourcePanel(resourceId);
        resourcePanels.put(resourceId, panel);
        resourcesPanel.add(panel);
        resourcesPanel.revalidate();
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public void setSyncEnabled(boolean enabled) {
        syncEnabledCheckbox.setSelected(enabled);
    }

    public void accessResource(int containerId, String resourceId, boolean withSync) {
        ResourcePanel panel = resourcePanels.get(resourceId);
        if (panel != null) {
            // Add container to resource
            boolean conflict = panel.addContainer(containerId);

            // If not using sync and we have a conflict, record it
            if (!withSync && conflict) {
                conflictCount.incrementAndGet();
                panel.flashConflict();
                updateStats();
            }

            // Force immediate repaint
            panel.repaint();
            this.repaint();

        }
    }

    public void releaseResource(int containerId, String resourceId)
    {
        ResourcePanel panel = resourcePanels.get(resourceId);
        if (panel != null) {
            panel.removeContainer(containerId);

            panel.repaint();
            this.repaint();
        }
    }

    public void recordOperation() {
        operationCount.incrementAndGet();
        updateStats();
    }

    public void recordConflict(int containerId, String resourceId) {
        conflictCount.incrementAndGet();
        ResourcePanel panel = resourcePanels.get(resourceId);
        if (panel != null) {
            panel.flashConflict();
        }
        updateStats();
    }

    public void updateResponseTime(long responseTime) {
        int currentAvg = avgResponseTime.get();
        int count = operationCount.get();

        if (count > 0) {
            // Calculate new running average
            int newAvg = (currentAvg * (count - 1) + (int)responseTime) / count;
            avgResponseTime.set(newAvg);
            updateStats();
        }
    }

    private void startPeriodicUpdates()
    {
        Timer timer = new Timer(1000, e -> {
            // Force repaint
            repaint();

            // Make some UI noise to ensure we're alive
            System.out.println("UI Update: " +
                    operationCount.get() + " operations, " +
                    conflictCount.get() + " conflicts");
        });
        timer.start();
    }

    private void updateStats() {
        // Calculate throughput
        long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSec > 0) {
            throughput.set(operationCount.get() / (int)elapsedSec);
        }

        // Update stats label
        statsLabel.setText(String.format(
                "Operations: %d | Conflicts: %d | Throughput: %d ops/sec | Avg Response: %dms",
                operationCount.get(),
                conflictCount.get(),
                throughput.get(),
                avgResponseTime.get()
        ));
    }

    // Inner class for resource panels
    class ResourcePanel extends JPanel {
        private String resourceId;
        private List<Integer> activeContainers = new ArrayList<>();
        private boolean hasConflict = false;
        private int conflictTimer = 0;
        private final int MAX_CAPACITY = 3; // Maximum allowed containers

        public ResourcePanel(String resourceId) {
            this.resourceId = resourceId;
            setBorder(BorderFactory.createTitledBorder(resourceId));
        }

        public boolean addContainer(int containerId)
        {
            if (!activeContainers.contains(containerId))
            {
                activeContainers.add(containerId);
            }
            activeContainers.add(containerId);
            repaint();
            return activeContainers.size() > MAX_CAPACITY;
        }

        public void removeContainer(int containerId) {
            activeContainers.remove(Integer.valueOf(containerId));
            repaint();
        }

        public void flashConflict() {
            hasConflict = true;
            conflictTimer = 10; // Flash for ~1 second
            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Update conflict status
            if (hasConflict && conflictTimer > 0) {
                conflictTimer--;
                if (conflictTimer == 0) {
                    hasConflict = false;
                }
            }

            // Set background color based on conflict status
            if (hasConflict) {
                g.setColor(new Color(255, 200, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.setColor(hasConflict ? Color.RED : new Color(220, 230, 240));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw containers
            // Draw each container very visibly
            int x = 20;
            int y = 30;
            for (Integer containerId : activeContainers)
            {
                // Draw a very visible box
                g.setColor(new Color(0, 100, 200));
                g.fillRect(x, y, 100, 50);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("C" + containerId, x + 40, y + 30);

                x += 110;
                if (x > getWidth() - 120) {
                    x = 20;
                    y += 60;
                }
            }

            // Draw capacity info
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Active: " + activeContainers.size() + "/" + MAX_CAPACITY, 20, getHeight() - 20);

            // Draw conflict warning if needed
            if (hasConflict) {
                g.setColor(Color.RED);
                g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
                g.drawString("CONFLICT!", getWidth() - 100, 20);
            }

        }
    }
}