# Cloud OS Process Synchronization Simulation

This project simulates a **cloud environment** where multiple containers (processes) access a shared cloud resource. 
It applies synchronization to avoid race conditions and maintain consistency in high-concurrency conditions.

This simulation was developed as part of a university assignment for **Process Synchronization in Cloud Operating Systems**.

## Objective
- Implement a synchronization strategy using **custom semaphores**
- Simulate concurrent access to shared cloud resources
- Collect and analyze performance metrics like throughput and response time

## Technologies Used
- **Java**
- Custom Semaphores (`Semaphore` package)
- Multithreading
- CSV & Excel output for performance metrics

## Synchronization Strategy
The simulation uses a **custom semaphore-based synchronization** strategy to coordinate access between multiple simulated containers. 
This prevents data inconsistencies and ensures only one container accesses critical sections at a time.

##  Performance Metrics
- **System Throughput**: Measured via `MetricsCollector.java`
- **Average Response Time**
- Results stored in:  
  - `Results/burst_test_results.csv`  
  - `Results/burst_test_results.xlsx`

## How to Run
1. Open the project in **IntelliJ IDEA** (Maven project)
2. Navigate to `DemoRunner.java` or `WorkloadSimulation.java`
3. Run the simulation
4. Check `Results/` folder for output metrics
---

## Author
**Annah Cebisile Masombuka**  
Final-year Computer Science Student  
University of Limpopo  
[LinkedIn](https://www.linkedin.com/in/cebisilemasombuka/)
