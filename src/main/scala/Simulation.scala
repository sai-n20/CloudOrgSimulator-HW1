package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, SchedulerComparisons, AllocationComparisons, NetworkSimulation, CostSimulation, ModelSimulations}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    SchedulerComparisons.Start()
//    AllocationComparisons.Start()
//    NetworkSimulation.Start()
//    CostSimulation.Start()
//    ModelSimulations.Start()
    logger.info("Finished cloud simulation...")

class Simulation