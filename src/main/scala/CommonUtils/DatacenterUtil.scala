package CommonUtils

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler
import org.slf4j.{Logger, LoggerFactory}
import CommonUtils.{hostUtil, vmUtil}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.{AggregateSwitch, EdgeSwitch, RootSwitch}
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}

import java.util
import java.util.{ArrayList, List}
import java.util.stream.IntStream
import scala.jdk.CollectionConverters.*


//Constructor receives config file name and uses it to initialize various datacenter parameters
//Moreover, for the final simulation task, there is an optional parameter for user-defined configuration
//This optional parameter ensures that locked configuration gets configured seperately from the user defined configuration
class DatacenterUtil(configFile: String, host: hostUtil, vm: vmUtil, cloudlet: cloudletUtil, userConfigFile: String = "") {

  //Create logger instance
  private val logger = LoggerFactory.getLogger(classOf[DatacenterUtil].getSimpleName)

  val conf: Config = ConfigFactory.load(configFile).getConfig("entryPoint.datacenter")

  // "If-else" statements in Scala are expressions which return value
  val vms: Int = {
    if (userConfigFile != "" && userConfigFile.endsWith(".conf")) {
      val confUser: Config = ConfigFactory.load(userConfigFile).getConfig("entryPoint.datacenter")
      confUser.getInt("vmAmount")
    }
    else {
      conf.getInt("vmAmount")
    }
  }

  val cloudlets: Int = {
    if (userConfigFile != "" && userConfigFile.endsWith(".conf")) {
      val confUser: Config = ConfigFactory.load(userConfigFile).getConfig("entryPoint.datacenter")
      confUser.getInt("cloudletsAmount")
    }
    else {
      conf.getInt("cloudletsAmount")
    }
  }

  val hosts = conf.getInt("hostsAmount")

  val costByMemory = conf.getDouble("costByMemory")
  val costBySec = conf.getDouble("costBySec")
  val costByBW = conf.getDouble("costByBW")
  val costByStorage = conf.getDouble("costByStorage")
  val arch = conf.getString("arch")
  val os = conf.getString("os")
  val vmm = conf.getString("vmm")
  val dcType = conf.getString("type")
  

  //Loop through the required amount of PE's from the host config and create a simple PE
  def makePEs: util.List[Pe] = {
    val PElist = new util.ArrayList[Pe]
    (1 to host.PE).map {_ =>
      PElist.add(new PeSimple(host.mips))
    }
    logger.info("{} PE(s) successfully created", host.PE)
    return PElist
  }

  //Make a host for a datacenter with the provided VM scheduler. The host is not powered on by default.
  @throws[InstantiationException]
  @throws[IllegalAccessException]
  def makeHost(vmScheduler: VmScheduler): Host = {
    val PElist = makePEs
    val scheduler = vmScheduler.getClass().getDeclaredConstructor().newInstance()
    return new NetworkHost(host.RAM, host.bw, host.storage, PElist).setVmScheduler(scheduler)
  }

  //Loop through the host amounts required in a specific datacenter
  def makeAllHosts(vmScheduler: VmScheduler): util.List[Host] = {
    val hostList = new util.ArrayList[Host]
    logger.info("Host creation for datacenter en masse")
    (1 to hosts).map {_ =>
      try hostList.add(makeHost(vmScheduler))
      catch {
        case e@(_: InstantiationException | _: IllegalAccessException) =>
          e.printStackTrace()
      }
    }
    logger.info("{} host(s) successfully created", hosts)
    return hostList
  }

  //Used the function from the NetworkVmExampleAbstract from cloudsimplus examples on github
  def getSwitchIndex(host: NetworkHost, ports: Int): Int = {
    return Math.round(host.getId % Int.MaxValue) / ports
  }

  def createNetworkTopology(sim: CloudSim, datacenter: NetworkDatacenter): Unit = {

    // Setting the network topology did not work for me at all, I couldn't figure out why. So I went the manual way of edge and root switches

    // val networkTopology: NetworkTopology = BriteNetworkTopology.getInstance("topology.brite")
    // sim.setNetworkTopology(networkTopology)

    //Create the required amount of edge switches as specified in the config file
    val edgeSwitches: List[EdgeSwitch] = new util.ArrayList[EdgeSwitch]
    (1 to conf.getInt("edgeSwitch.amount")).map {
      e =>
        val edgeSwitch: EdgeSwitch = new EdgeSwitch(sim, datacenter)
        edgeSwitch.setPorts(conf.getInt("edgeSwitch.ports"))
        edgeSwitch.setUplinkBandwidth(conf.getInt("edgeSwitch.bw"))
        edgeSwitch.setSwitchingDelay(conf.getInt("edgeSwitch.switchingDelay"))
        edgeSwitch.setDownlinkBandwidth(conf.getInt("edgeSwitch.bw"))
        datacenter.addSwitch(edgeSwitch)
        edgeSwitches.add(edgeSwitch)
    }
    logger.info("{} edge switch(es) successfully created", conf.getInt("edgeSwitch.amount"))

    //Create the required amount of aggregate switches as specified in the config file
    val aggregateSwitches: List[AggregateSwitch] = new util.ArrayList[AggregateSwitch]
    (1 to conf.getInt("aggregateSwitch.amount")).map {
      e =>
        val aggregateSwitch: AggregateSwitch = new AggregateSwitch(sim, datacenter)
        aggregateSwitch.setPorts(conf.getInt("aggregateSwitch.ports"))
        aggregateSwitch.setUplinkBandwidth(conf.getInt("aggregateSwitch.bw"))
        aggregateSwitch.setSwitchingDelay(conf.getInt("aggregateSwitch.switchingDelay"))
        aggregateSwitch.setDownlinkBandwidth(conf.getInt("aggregateSwitch.bw"))
        datacenter.addSwitch(aggregateSwitch)
        aggregateSwitches.add(aggregateSwitch)
    }
    logger.info("{} aggregate switch(es) successfully created", conf.getInt("aggregateSwitch.amount"))

    //Create a root switch and set its configuration
    val rootSwitch: RootSwitch = new RootSwitch(sim, datacenter)
    rootSwitch.setPorts(conf.getInt("rootSwitch.ports"))
    rootSwitch.setUplinkBandwidth(conf.getInt("rootSwitch.bw"))
    rootSwitch.setSwitchingDelay(conf.getInt("rootSwitch.switchingDelay"))
    rootSwitch.setDownlinkBandwidth(conf.getInt("rootSwitch.bw"))
    datacenter.addSwitch(rootSwitch)
    logger.info("{} root switch(es) successfully created", conf.getInt("rootSwitch.amount"))

    //Connecting edge switches to hosts
    datacenter.getHostList[NetworkHost].forEach(e => {
      val switchIndex = getSwitchIndex(e, conf.getInt("edgeSwitch.ports"))
      edgeSwitches.get(switchIndex).connectHost(e)
    })
    logger.info("Connected {} edge switch(es) to {} hosts", conf.getInt("edgeSwitch.amount"), hosts)

    //Connect root switches to aggregate switches
    for (aggregateSwitch <- aggregateSwitches.asScala) {
      aggregateSwitch.getUplinkSwitches.add(rootSwitch)
      rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
    }
    logger.info("Connected {} aggregate switch(es) to {} root switch(es)", conf.getInt("aggregateSwitch.amount"), conf.getInt("rootSwitch.amount"))
  }

  //Creates a datacenter for the provided CloudSim instance. VM allocation policy and VM scheduler are also provided to be passed on to the datacenter and the host respectively
  def createDatacenter(sim: CloudSim, vmAllocationPolicy: VmAllocationPolicy, vmScheduler: VmScheduler): Datacenter = {
    val hostList = makeAllHosts(vmScheduler)

    //Switch case for a network datacenter as specified in config file
    val datacenter = dcType match {
      case "Simple" => new DatacenterSimple(sim, hostList, vmAllocationPolicy)
      case "Network" => new NetworkDatacenter(sim, hostList, vmAllocationPolicy)
      case _ => new DatacenterSimple(sim, hostList, vmAllocationPolicy)
    }
    if (dcType == "Network")
      createNetworkTopology(sim, datacenter.asInstanceOf[NetworkDatacenter])
    datacenter.getCharacteristics
      .setVmm(vmm)
      .setCostPerBw(costByBW)
      .setCostPerMem(costByMemory)
      .setCostPerSecond(costBySec)
      .setCostPerStorage(costByStorage)
      .setArchitecture(arch)
      .setOs(os)
    logger.info("Created a {} datacenter", dcType)
    return datacenter
  }
}
