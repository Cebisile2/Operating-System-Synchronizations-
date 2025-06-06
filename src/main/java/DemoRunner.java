import javax.swing.*;

public class DemoRunner {
    public static void main(String[] args) {
        SimulationConfig syncConfig = new SimulationConfig();
        syncConfig.numContainers = 10;
        syncConfig.numResources = 5;
        syncConfig.simulationTimeSeconds = 120;
        syncConfig.enableSynchronization = true;
        syncConfig.maxConcurrentAccess = 3;

        SimulationConfig noSyncConfig = new SimulationConfig();
        noSyncConfig.numContainers = 10;
        noSyncConfig.numResources = 5;
        noSyncConfig.simulationTimeSeconds = 120;
        noSyncConfig.enableSynchronization = false;
        noSyncConfig.maxConcurrentAccess = 3;

        System.out.println("Creating simulations...");

        SwingWorker<Void, Void> syncWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                CloudSyncSimulation syncSim = new CloudSyncSimulation(syncConfig);
                syncSim.setup();
                syncSim.run();
                return null;
            }

            @Override
            protected void done() {
                System.out.println("Synchronized simulation complete.");
            }
        };

        SwingWorker<Void, Void> noSyncWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                CloudSyncSimulation noSyncSim = new CloudSyncSimulation(noSyncConfig);
                noSyncSim.setup();
                noSyncSim.run();
                return null;
            }

            @Override
            protected void done() {
                System.out.println("Non-synchronized simulation complete.");
            }
        };

        System.out.println("Starting simulations...");
        syncWorker.execute();
        noSyncWorker.execute();

        try {
            Thread.currentThread().join(); // 🛑 Prevents JVM from exiting
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
