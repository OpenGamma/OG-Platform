/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.analytics.financial.credit.underlyingpool.UnderlyingPoolDummyPool;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ScenarioGeneratorTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int numberOfSimulations = 12;

  private static final int defaultSimulationSeed = 987654321;
  private static final int recoveryRateSimulationSeed = 987654321;

  private static final double simulationTimeHorizon = 1.0;

  private static final double homogeneousDefaultCorrelation = 0.5;
  private static final double homogeneousRecoveryCorrelation = 0.0;
  private static final double homogeneousDefaultProbability = 0.5;

  // Create a pool construction object
  private static final UnderlyingPoolDummyPool pool = new UnderlyingPoolDummyPool();

  // Build the underlying pool
  private static final UnderlyingPool obligorUniverse = pool.constructPool();

  // Extract the number of obligors in the simulation universe from the UnderlyingPool
  private static final int numberOfObligors = obligorUniverse.getNumberOfObligors();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final SimulationMethods simMethods = new SimulationMethods();

  private static final SimulationEngine simulationEngine = new SimulationEngine();

  private static final RecoveryRateModel[] recoveryRateModels = simMethods.constructRecoveryRateModels(numberOfObligors);

  private static final double[] defaultCorrelationVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousDefaultCorrelation);
  private static final double[] recoveryCorrelationVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousRecoveryCorrelation);
  private static final double[] defaultProbabilityVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousDefaultProbability);

  private static final ScenarioGenerator scenarioGenerator = new ScenarioGenerator(
      obligorUniverse,
      recoveryRateModels,
      numberOfSimulations,
      defaultSimulationSeed,
      recoveryRateSimulationSeed,
      simulationTimeHorizon,
      defaultCorrelationVector,
      recoveryCorrelationVector,
      defaultProbabilityVector);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testCreditPortfolioLossModelScenarioGenerator() {

    simulationEngine.runSimulation(scenarioGenerator, outputResults);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
