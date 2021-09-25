package Simulations

import CommonUtils.{DatacenterUtil, cloudletUtil, hostUtil, vmUtil}
import HelperUtils.CreateLogger
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}

import java.util

class ModelSimulations

object ModelSimulations:
  def Start(): Unit = {

    //Import configuration and create logger instance
    val IaaSLockedconfig: Config = ConfigFactory.load("IaaSLocked.conf")
    val IaaSUserconfig: Config = ConfigFactory.load("IaaSUser.conf")
    val PaaSLockedconfig: Config = ConfigFactory.load("PaaSLocked.conf")
    val PaaSUserconfig: Config = ConfigFactory.load("PaaSUser.conf")
    val SaaSLockedconfig: Config = ConfigFactory.load("SaaSLocked.conf")
    val SaaSUserconfig: Config = ConfigFactory.load("SaaSUser.conf")
    val logger = CreateLogger(classOf[ModelSimulations])


    //Initialize required instance members
    val cloudsimModel = new CloudSim()

    //Create instance members of required hardware for the datacenter
    val hostConfigIaaS = new hostUtil("IaaSLocked.conf")
    val vmConfigIaaS = new vmUtil("IaaSLocked.conf", "IaaSUser.conf")
    val cloudletConfigIaaS = new cloudletUtil("IaaSUser.conf")
    val datacenterConfigIaaS = new DatacenterUtil("IaaSLocked.conf", hostConfigIaaS, vmConfigIaaS, cloudletConfigIaaS, "IaaSUser.conf")

    val hostConfigPaaS = new hostUtil("PaaSLocked.conf")
    val vmConfigPaaS = new vmUtil("PaaSLocked.conf")
    val cloudletConfigPaaS = new cloudletUtil("PaaSUser.conf")
    val datacenterConfigPaaS = new DatacenterUtil("PaaSLocked.conf", hostConfigPaaS, vmConfigPaaS, cloudletConfigPaaS, "PaaSUser.conf")

    val hostConfigSaaS = new hostUtil("SaaSLocked.conf")
    val vmConfigSaaS = new vmUtil("SaaSLocked.conf")
    val cloudletConfigSaaS = new cloudletUtil("SaaSUser.conf")
    val datacenterConfigSaaS = new DatacenterUtil("SaaSLocked.conf", hostConfigSaaS, vmConfigSaaS, cloudletConfigSaaS)

    //Create datacenter from the util file with the required VM parameters
    datacenterConfigIaaS.createDatacenter(cloudsimModel, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    datacenterConfigPaaS.createDatacenter(cloudsimModel, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    datacenterConfigSaaS.createDatacenter(cloudsimModel, new VmAllocationPolicyBestFit, new VmSchedulerSpaceShared())
    val brokerModel = new DatacenterBrokerSimple(cloudsimModel)

    //Invoke the vmUtil class to create and configure the VM's per the config file
    val vmIaaS: util.List[Vm] = vmConfigIaaS.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfigIaaS.vms)
    val vmPaaS: util.List[Vm] = vmConfigPaaS.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfigPaaS.vms)
    val vmSaaS: util.List[Vm] = vmConfigSaaS.makeVMs(new CloudletSchedulerSpaceShared(), datacenterConfigSaaS.vms)
    vmIaaS.addAll(vmPaaS)
    vmIaaS.addAll(vmSaaS)
    brokerModel.submitVmList(vmIaaS)

    //Invoke the cloudletUtil class to create and configure the cloudlets per the config file
    val cloudletsIaaS: util.List[Cloudlet] = cloudletConfigIaaS.makeCloudlets(new UtilizationModelFull(), datacenterConfigIaaS.cloudlets)
    val cloudletsPaaS: util.List[Cloudlet] = cloudletConfigPaaS.makeCloudlets(new UtilizationModelFull(), datacenterConfigPaaS.cloudlets)
    val cloudletsSaaS: util.List[Cloudlet] = cloudletConfigSaaS.makeCloudlets(new UtilizationModelFull(), datacenterConfigSaaS.cloudlets)
    cloudletsIaaS.addAll(cloudletsPaaS)
    cloudletsIaaS.addAll(cloudletsSaaS)
    brokerModel.submitCloudletList(cloudletsIaaS)

    //Start the simulation
    cloudsimModel.start()

    //Get a list of the completed cloudlets to display
    val completedCostCloudlets: util.List[Cloudlet] = brokerModel.getCloudletFinishedList
    new CloudletsTableBuilder(completedCostCloudlets)
      .addColumn(new TextTableColumn("RAM Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfRam() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("CPU Utilization", "Percentage"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getUtilizationOfCpu() * 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .addColumn(new TextTableColumn("Total Cost", "USD"), (cloudlet: Cloudlet) => BigDecimal(cloudlet.getTotalCost()).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .build()
  }
