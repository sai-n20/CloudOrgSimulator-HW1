package CommonUtils

import com.typesafe.config.{Config, ConfigFactory}

class hostUtil(configFile: String) {

  //Constructor receives config file name and uses it to initialize host parameters
  val conf: Config = ConfigFactory.load(configFile).getConfig("entryPoint.datacenter.hosts")
  val PE = conf.getInt("cores")
  val mips = conf.getInt("mips")
  val bw = conf.getInt("bw")
  val RAM = conf.getInt("RAM")
  val storage = conf.getInt("storageGB")

}
