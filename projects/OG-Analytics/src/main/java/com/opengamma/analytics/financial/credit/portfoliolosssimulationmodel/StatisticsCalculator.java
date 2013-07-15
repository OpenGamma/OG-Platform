/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

/**
 * Class to extract statistics from the simulated scenarios 
 */
public class StatisticsCalculator {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : 

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public StatisticsCalculator() {

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to compute the number of simulated defaults in each simulation

  public int[] getNumberOfDefaultsPerScenario(final ScenarioGenerator scenarioGenerator, final SimulatedObligorDefaultState[][] simulatedDefaultScenarios) {

    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    int[] numberOfDefaultsPerScenario = new int[numberOfSimulations];

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      int numberOfDefaults = 0;

      for (int i = 0; i < numberOfObligors; i++) {
        if (simulatedDefaultScenarios[alpha][i] == SimulatedObligorDefaultState.DEFAULTED) {
          numberOfDefaults++;
        }
      }

      numberOfDefaultsPerScenario[alpha] = numberOfDefaults;
    }
    return numberOfDefaultsPerScenario;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
