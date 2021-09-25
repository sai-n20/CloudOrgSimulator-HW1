package Simulations

import CommonUtils.{DatacenterUtil, cloudletUtil, hostUtil, vmUtil}
import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigBeanFactory, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull, UtilizationModelStochastic}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TableColumn, TextTableColumn}

import java.util
import scala.jdk.CollectionConverters.*

class SchedulerComparisons

object SchedulerComparisons:
  def Start(): Unit = {

    //Import configuration and create logger instance
    val config: Config = ConfigFactory.load("SchedulerComparisons.conf")
    val logger = CreateLogger(classOf[SchedulerComparisons])
    logger.info(s"Test config loading, description is: ${config.getString("entryPoint.description")}")

    //Create a filename value for easier substitution
    val configFileName: String = "SchedulerComparisons.conf"

    //Initialize required instance members
    val cloudsimSpace = new CloudSim()

    //Create instance members of required hardware for the datacenter
    val hostConfig = new hostUtil(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val cloudletConfig = new cloudletUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, vmConfig, cloudletConfig)

    logger.info("Beginning task one, first is Space Shared VM Scheduling Simulation")
    logger.info("In this simulation, we use 1 VM and 1 Host which will accept 2 cloudlets for execution")

    //Create datacenter from the util file with the required VM parameters
    datacenterConfig.createDatacenter(cloudsimSpace, new VmAllocationPolicySimple, new VmSchedulerSpaceShared)
    val brokerSpace = new DatacenterBrokerSimple(cloudsimSpace)

    //Invoke the vmUtil class to create and configure the VM's per the config file
    val vmSpace: Vm = vmConfig.makeVM(new CloudletSchedulerSpaceShared())
    brokerSpace.submitVm(vmSpace)

    //Invoke the cloudletUtil class to create and configure the cloudlets per the config file
    val cloudletsSpace: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), datacenterConfig.cloudlets)
    brokerSpace.submitCloudletList(cloudletsSpace)

    //Start the simulation
    cloudsimSpace.start()

    //Get a list of the completed cloudlets to display
    val completedSpaceCloudlets: util.List[Cloudlet] = brokerSpace.getCloudletFinishedList
    new CloudletsTableBuilder(completedSpaceCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()

    logger.info("Task one continued, using Time Shared VM Scheduling now with stochastic utilization model for cloudlets")

    val cloudsimTime = new CloudSim()
    datacenterConfig.createDatacenter(cloudsimTime, new VmAllocationPolicySimple, new VmSchedulerTimeShared)
    val brokerTime = new DatacenterBrokerSimple(cloudsimTime)

    val vmTime: Vm = vmConfig.makeVM(new CloudletSchedulerTimeShared())
    brokerTime.submitVm(vmTime)
    val cloudletsTime: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelStochastic(), datacenterConfig.cloudlets)
    brokerTime.submitCloudletList(cloudletsTime)
    cloudsimTime.start()

    val completedTimeCloudlets: util.List[Cloudlet] = brokerTime.getCloudletFinishedList
    new CloudletsTableBuilder(completedTimeCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()
  }
