package customtestsuite

import CommonUtils.{DatacenterUtil, cloudletUtil, hostUtil, vmUtil}
import Simulations.BasicCloudSimPlusExample.config
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.Vm
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

import java.util
import java.util.List

class datacenterTest extends AnyFunSuite {

  test("Unit test to see if a VM instance is created") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val vm: util.List[Vm] = vmConfig.makeVMs(new CloudletSchedulerSpaceShared(), config.getInt("entryPoint.vmAmount"))
    vm shouldBe a [List[Vm]]
    vm.get(0) shouldBe a [Vm]
  }

  test("Unit test to see if datacenter is created") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val hostConfig = new hostUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, new vmUtil(configFileName), new cloudletUtil(configFileName))
    val datacenter = datacenterConfig.createDatacenter(new CloudSim(), new VmAllocationPolicySimple(), new VmSchedulerSpaceShared())
    datacenter shouldBe a [Datacenter]
  }

  test("Unit test to see if host(s) are created") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val hostConfig = new hostUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, new vmUtil(configFileName), new cloudletUtil(configFileName))
    val hostList = datacenterConfig.makeAllHosts(new VmSchedulerSpaceShared())
    hostList shouldBe a [List[Host]]
    hostList.get(0) shouldBe a [Host]
  }

  test("Unit test to see if VM scheduler is set correctly") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val hostConfig = new hostUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, new vmUtil(configFileName), new cloudletUtil(configFileName))
    val hostList = datacenterConfig.makeAllHosts(new VmSchedulerSpaceShared())
    val hostScheduler = hostList.get(0).getVmScheduler()
    hostScheduler shouldBe a [VmSchedulerSpaceShared]
  }

  test("Unit test to see if all VM's are allocated") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val hostConfig = new hostUtil(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val cloudletConfig = new cloudletUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, vmConfig, cloudletConfig)
    val datacenter = datacenterConfig.createDatacenter(new CloudSim(), new VmAllocationPolicySimple(), new VmSchedulerSpaceShared())
    val cloudsim = new CloudSim()
    datacenterConfig.createDatacenter(cloudsim, new VmAllocationPolicySimple, new VmSchedulerSpaceShared)
    val broker = new DatacenterBrokerSimple(cloudsim)

    val vm: util.List[Vm] = vmConfig.makeVMs(new CloudletSchedulerSpaceShared(), config.getInt("entryPoint.vmAmount"))
    broker.submitVmList(vm)
    val cloudlets: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), config.getInt("entryPoint.cloudletsAmount"))
    broker.submitCloudletList(cloudlets)
    cloudsim.start()

    broker.getVmCreatedList().size() shouldEqual vm.size()
  }

  test("Unit test to see if all cloudlets are executed") {
    val configFileName: String = "SchedulerComparisons.conf"
    val config: Config = ConfigFactory.load(configFileName)
    val hostConfig = new hostUtil(configFileName)
    val vmConfig = new vmUtil(configFileName)
    val cloudletConfig = new cloudletUtil(configFileName)
    val datacenterConfig = new DatacenterUtil(configFileName, hostConfig, vmConfig, cloudletConfig)
    val datacenter = datacenterConfig.createDatacenter(new CloudSim(), new VmAllocationPolicySimple(), new VmSchedulerSpaceShared())
    val cloudsim = new CloudSim()
    datacenterConfig.createDatacenter(cloudsim, new VmAllocationPolicySimple, new VmSchedulerSpaceShared)
    val broker = new DatacenterBrokerSimple(cloudsim)

    val vm: Vm = vmConfig.makeVM(new CloudletSchedulerSpaceShared())
    broker.submitVm(vm)
    val cloudlets: util.List[Cloudlet] = cloudletConfig.makeCloudlets(new UtilizationModelFull(), config.getInt("entryPoint.cloudletsAmount"))
    broker.submitCloudletList(cloudlets)
    cloudsim.start()

    val completedCloudlets: util.List[Cloudlet] = broker.getCloudletFinishedList
    cloudlets.size() shouldEqual completedCloudlets.size()
  }
}
