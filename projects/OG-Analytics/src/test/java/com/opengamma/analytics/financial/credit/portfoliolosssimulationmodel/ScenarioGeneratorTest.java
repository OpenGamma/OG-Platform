/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.analytics.financial.credit.underlyingpool.UnderlyingPoolDummyPool;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;

/**
 * 
 */
public class ScenarioGeneratorTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int numberOfSimulations = 7;

  private static final int simulationSeed = 987654321;

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

  private static final RecoveryRateModel[] recoveryRateModels = simMethods.constructRecoveryRateModels(numberOfObligors);

  private static final double[] defaultCorrelationVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousDefaultCorrelation);
  private static final double[] recoveryCorrelationVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousRecoveryCorrelation);
  private static final double[] defaultProbabilityVector = simMethods.constructCorrelationVector(numberOfObligors, homogeneousDefaultProbability);

  private static final ScenarioGenerator scenarioGenerator = new ScenarioGenerator(
      obligorUniverse,
      recoveryRateModels,
      numberOfSimulations,
      simulationSeed,
      simulationTimeHorizon,
      defaultCorrelationVector,
      recoveryCorrelationVector,
      defaultProbabilityVector);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //@Test
  public void testScenarioGenerator() {

    final int[][] simulatedDefaultScenarios = scenarioGenerator.generateDefaultScenarios(scenarioGenerator);

    final int[] numberOfDefaultsPerScenario = scenarioGenerator.getNumberOfDefaultsPerScenario(scenarioGenerator, simulatedDefaultScenarios);

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
      for (int i = 0; i < numberOfObligors; i++) {
        System.out.print(simulatedDefaultScenarios[alpha][i] + "\t");
      }
      System.out.println();
    }

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
      System.out.println("Scenario alpha = " + alpha + "\t" + numberOfDefaultsPerScenario[alpha]);
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
