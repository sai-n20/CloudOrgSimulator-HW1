package CommonUtils

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import java.util
import java.util.{ArrayList, List}


//Constructor receives config file name and uses it to initialize VM parameters. Optionally for IaaS, user can configure various parameters for their VM's
class vmUtil(configFile: String, userConfigFile: String = "") {

  // "If-else" statements in Scala are expressions which return value
  val conf: Config = {
    if (userConfigFile != "" && userConfigFile.endsWith(".conf")) {
      ConfigFactory.load(userConfigFile).getConfig("entryPoint.datacenter.vm")
    }
    else {
      ConfigFactory.load(configFile).getConfig("entryPoint.datacenter.vm")
    }
  }

  val PE = conf.getInt("cores")
  val mips = conf.getInt("mips")
  val bw = conf.getInt("bw")
  val RAM = conf.getInt("RAM")
  val vmm = conf.getString("vmm")
  val size = conf.getInt("size")


  //Create a VM to be placed in a host with a parameterized cloudlet scheduler
  def makeVM(cloudletScheduler: CloudletScheduler): Vm = return new VmSimple(mips, PE, cloudletScheduler).setBw(bw).setRam(RAM).setSize(size)

  //Loop through the required amount of VM's as specified in the config file
  def makeVMs(cloudletScheduler: CloudletScheduler, amount: Int): util.List[Vm] = {
    val vmList = new util.ArrayList[Vm]
    (1 to amount).map {_ =>
      vmList.add(makeVM(cloudletScheduler))
    }
    return vmList
  }
}
