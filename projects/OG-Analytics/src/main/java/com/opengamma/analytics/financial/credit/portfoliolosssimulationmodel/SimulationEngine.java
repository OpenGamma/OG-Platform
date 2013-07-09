/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

/**
 * Class to call the different steps of the simulation engine e.g. generate scenarios, reval positions and compute statistics
 */
public class SimulationEngine {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the portfolio of trades object
  // TODO : Remove the outputResults flag when finished testing

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Default ctor
  public SimulationEngine() {

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to run a simulation model

  public void runSimulation(final ScenarioGenerator scenarioGenerator, final boolean outputResults) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Step I - Scenario generation

    // Generate the simulated defaults
    final SimulatedObligorDefaultState[][] simulatedDefaultScenarios = scenarioGenerator.generateDefaultScenarios(scenarioGenerator);

    // Generate the simulated recovery rates per default
    final double[][] simulatedRecoveryRateScenarios = scenarioGenerator.generateRecoveryRateScenarios(scenarioGenerator, simulatedDefaultScenarios);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Step II - Position revaluation

    // Reval the portfolio of trades given the simulated scenarios

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Step III - Analysis

    // Compute the simulated P/L statistics

    // Compute the number of simulated defaults per scenario
    final int[] numberOfDefaultsPerScenario = statisticsCalculator.getNumberOfDefaultsPerScenario(scenarioGenerator, simulatedDefaultScenarios);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Step IV - Reporting

    final int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    final int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    if (outputResults) {
      for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
        System.out.print("Scenario " + alpha + "\t\t");
        for (int i = 0; i < numberOfObligors; i++) {
          System.out.print(simulatedDefaultScenarios[alpha][i] + "\t");
        }
        System.out.println();
      }

      System.out.println();

      for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
        System.out.println("Scenario alpha = " + alpha + "\t" + numberOfDefaultsPerScenario[alpha]);
      }

      System.out.println();

      for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
        for (int i = 0; i < numberOfObligors; i++) {
          System.out.print(simulatedRecoveryRateScenarios[alpha][i] + "\t");
        }
        System.out.println();
      }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
