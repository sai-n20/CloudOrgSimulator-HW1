package Simulations

import CommonUtils.{DatacenterUtil, cloudletUtil, hostUtil, vmUtil}
import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigBeanFactory, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull, UtilizationModelStochastic}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}

import java.util
import scala.jdk.CollectionConverters.*

class NetworkSimulation

object NetworkSimulation:
  def Start(): Unit = {

    //Import configuration and create logger instance
    val config: Config = ConfigFactory.load("NetworkSimulation.conf")
    val logger = CreateLogger(classOf[NetworkSimulation])
    logger.info(s"Test config loading, description is: ${config.getString("entryPoint.description")}")

    //Create a filename value for easier substitution
    val configFileName: String = "NetworkSimulation.conf"

    //Initialize required instance members
    val cloudsimNetwork = new CloudSim()

    //Create instance members of required hardware for the datacenter
    val hostConfig = new hostUtil(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val cloudletConfig = new cloudletUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, vmConfig, cloudletConfig)

    //Create a network datacenter from the util file with the required VM parameters. The network topology is set from the util class
    datacenterConfig.createDatacenter(cloudsimNetwork, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    val brokerNetwork = new DatacenterBrokerSimple(cloudsimNetwork)

    //Invoke the vmUtil class to create and configure the VM's per the config file
    val vmNetwork: util.List[Vm] = vmConfig.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfig.vms)
    brokerNetwork.submitVmList(vmNetwork)

    //Invoke the cloudletUtil class to create and configure the cloudlets per the config file
    val cloudletsNetwork: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), datacenterConfig.cloudlets)
    brokerNetwork.submitCloudletList(cloudletsNetwork)

    //Start the simulation
    cloudsimNetwork.start()

    //Get a list of the completed cloudlets to display
    val completedNetworkCloudlets: util.List[Cloudlet] = brokerNetwork.getCloudletFinishedList
    new CloudletsTableBuilder(completedNetworkCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()
  }
