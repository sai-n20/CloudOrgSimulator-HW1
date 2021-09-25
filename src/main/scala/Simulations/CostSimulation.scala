package Simulations

import CommonUtils.{DatacenterUtil, cloudletUtil, hostUtil, vmUtil}
import HelperUtils.CreateLogger
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelFull, UtilizationModelStochastic}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}

import java.util

class CostSimulation

object CostSimulation:
  def Start(): Unit = {

    //Import configuration and create logger instance
    val config: Config = ConfigFactory.load("CostSimulation.conf")
    val logger = CreateLogger(classOf[CostSimulation])
    logger.info(s"Test config loading, description is: ${config.getString("entryPoint.description")}")

    //Create a filename value for easier substitution
    val configFileName: String = "CostSimulation.conf"

    //Initialize required instance members
    val cloudsimCost = new CloudSim()

    //Create instance members of required hardware for the datacenter
    val hostConfig = new hostUtil(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val cloudletConfig = new cloudletUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, vmConfig, cloudletConfig)

    //Create datacenter from the util file with the required VM parameters
    datacenterConfig.createDatacenter(cloudsimCost, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    val brokerCost = new DatacenterBrokerSimple(cloudsimCost)

    //Invoke the vmUtil class to create and configure the VM's per the config file
    val vmCost: util.List[Vm] = vmConfig.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfig.vms)
    brokerCost.submitVmList(vmCost)

    //Invoke the cloudletUtil class to create and configure the cloudlets per the config file
    val cloudletsCost: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), datacenterConfig.cloudlets)
    brokerCost.submitCloudletList(cloudletsCost)

    //Start the simulation
    cloudsimCost.start()

    //Get a list of the completed cloudlets to display
    val completedCostCloudlets: util.List[Cloudlet] = brokerCost.getCloudletFinishedList
    new CloudletsTableBuilder(completedCostCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("Total Cost", "USD"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()

    logger.info("Looking at an alternative datacenter now with a higher cost per storage but lower cost per second")

    val cloudsimAlternativeCost = new CloudSim()
    val datacenterAlternativeConfig = new DatacenterUtil("AlternativeCostSimulation.conf", hostConfig, vmConfig, cloudletConfig)
    datacenterAlternativeConfig.createDatacenter(cloudsimAlternativeCost, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    val brokerAlternativeCost = new DatacenterBrokerSimple(cloudsimAlternativeCost)
    val vmAlternativeCost: util.List[Vm] = vmConfig.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfig.vms)
    val cloudletsAlternativeCost: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), datacenterConfig.cloudlets)

    brokerAlternativeCost.submitVmList(vmAlternativeCost)
    brokerAlternativeCost.submitCloudletList(cloudletsAlternativeCost)
    cloudsimAlternativeCost.start()

    val completedAlternativeCostCloudlets: util.List[Cloudlet] = brokerAlternativeCost.getCloudletFinishedList
    new CloudletsTableBuilder(completedAlternativeCostCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("Total Cost", "USD"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()
  }
