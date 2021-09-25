package CommonUtils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel

import java.util
import java.util.{ArrayList, List}

class cloudletUtil(configFile: String) {

  //Constructor receives config file name and uses it to initialize cloudlet parameters
  val conf: Config = ConfigFactory.load(configFile).getConfig("entryPoint.datacenter.cloudlets")
  val PE = conf.getInt("cores")
  val timeLength = conf.getInt("timeLength")
  val size = conf.getInt("size")


  //Loop through the amount of cloudlets and create a Simple Cloudlet with the parameterized utilization model
  def makeCloudlets(utilModel: UtilizationModel, amount: Int): util.List[Cloudlet] = {
    val cloudletList = new util.ArrayList[Cloudlet]
    (1 to amount).map {_ =>
      cloudletList.add(new CloudletSimple(timeLength, PE, utilModel).setSizes(size))
    }
    return cloudletList
  }
}
