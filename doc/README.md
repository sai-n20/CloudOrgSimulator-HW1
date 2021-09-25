# CS 441 HW 1

## Introduction: This repository contains the programs necessary to run various simulations of datacenters using the popular CloudSimPlus framework. A total of 8 simulations have been written here in 5 files with distinct simulation themes.

## Project set up
+ Clone the project or download the repo in a zip format
+ Open a terminal at the root level of the repository
+ To run the test cases written for the simulations,

```
sbt clean compile test
```

+ To run the simulations themselves (all 8 of them one after the other)

```
sbt clean compile run
```

## Application Design

These are the main simulation files found in the src/scala/Simulations directory:-
+ `SchedulerComparison` is the first program written and consequently, executed by the run command. This program contains 2 simulations which form the basis for task one of the requirements of the homework. Here, the VM and Cloudlet scheduler policies are compared while keeping (almost) everything else constant. The cloudlet utilization model is the only thing changed here apart from the schedulers, which will be explained in the results section below.

+ `AllocationComparison` is the second program written and consequently, executed by the run command. This program also contains 2 simulations which form the basis for task two of the requirements of the homework. Here, the VM allocation policies are explored while keeping everything else constant. The allocation policies chosen were the Round Robin and Best Fit policies.

+ `NetworkSimulation` attempts to simulate a network datacenter. Here, a topology consisting of edge switches, aggregate switches and a root switch is used to connect hosts within the datacenter. A BestFit VM allocation policy is used to understand the scenario in terms of a network datacenter with switching delays involved.

+ `CostSimulation` attempts to model 2 datacenters with varying cost strategies. The 2 datacenters can be considered to be from different competitive cloud providers, who each have their strengths and weaknesses.

+ `ModelsSimulation` is the final task given in the homework for the main textbook owners. Here, I have implemented 3 datacenters each configured with IaaS, PaaS and SaaS idealogies. This is emulated through the use of 2 configuration files for each idealogy. There is a "locked" configuration file for each topology which contains all the necessary configuration parameters for setting up a simulation, while there is also a "user" configuration file which for each topology, indicating the elements of the datacenter they are in control of. For e,g- for a IaaS idealogy, the "locked" file contains all parameters to initialize, while the "user" file contains the various VM parameters (VM size, cores, storage, bandwidth, mips) which a user of a IaaS datacenter would configure by themselves. This strategy ensures seperation of control over things the user should have no control of, like cost values.

These simulation files have a lot of repeated actions in terms of host, datacenter, vm, cloudlet etc creation to name a few. Hence, to facilitate easier understanding and improving the codebase functionally, there is a seperate CommonUtils package created which houses the respective utility functions. They are:-
+ `cloudletUtil` is the utility file which reads cloudlet configuration and handles multiple cloudlet creation

+ `vmUtil` is the utility file to read VM configuration and creation of multiple VM's. This utility also handles reading VM configuration from appropriate configuration file in terms of the "locked" configuration and the "user" configuration for the various IaaS, PaaS and SaaS idealogies.

+ `hostUtil` is a short utility file to read host configuration.

+ `datacenterUtil` is a large utility file housing more of the utility code in terms of host and datacenter creation. The complexity comes in terms of handling the IaaS and PaaS idealogies, host creation and the selection between a NetworkDataCenter and a SimpleDataCenter. This utility file also contains a network topology mapper which creates and configures all the types of switches, namely the edge, aggregate and root switches. They are manually connected in the appropriate function.

The resources directory contains the respective configuration file for each of these simulations. The test cases are written in Scala using the AnyFunSuite and they are located in the test directoy where you'd expect them.

## Simulation results

1. Scheduler Comparisons- A single host, single VM and 2 cloudlets are utilized here to demonstrate the differences between the schedulers. To elaborate on a previous statement, the utilization model for the cloudlets was changed here as using the standard `UtilizationModelFull()` led to cloudlets waiting for RAM and Bandwidth in `TimeSharedScheduler` for the cloudlets. This makes sense as the first cloudlet to begin executing will take up all the resources, while the others will starve. This problem does not occur on `SpaceSharedScheduler` for the cloudlets.

<p align="center">
  <img src="https://raw.githubusercontent.com/sainadkarni/CloudOrgSimulator-HW1/main/doc/Scheduler1.jpg" style="width: 50%;" />
</p>
