/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate a set of simulated default/recovery rate scenarios for a specified universe of obligors
 */
public class ScenarioGenerator {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Do we need to do any arg checks on the simulation seed?
  // TODO : Remember to check if an obligor is already tagged as having defaulted
  // TODO : Check range of rho more carefully
  // TODO Sort out the simulation seed

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The universe of obligors to simulate scenarios for (treat as a very large 'UnderlyingPool' object)
  private final UnderlyingPool _obligorUniverse;

  // The recovery rate models (constant or stochastic) for each obligor in the universe of obligors
  private final RecoveryRateModel[] _recoveryRateModels;

  // The number of simulations
  private final int _numberOfSimulations;

  // The default simulation random number seed
  private final int _defaultSimulationSeed;

  // The time horizon for the simulation (usually an integer, but need not be)
  private final double _simulationTimeHorizon;

  // The correlation of the defaults with the systemic factor
  private final double[] _rho;

  // The correlation of the sampled recovery rates with the sysatemic factor
  private final double[] _beta;

  // The default probability of the obligors
  private final double[] _defaultProbability;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ScenarioGenerator(
      final UnderlyingPool obligorUniverse,
      final RecoveryRateModel[] recoveryRateModels,
      final int numberOfSimulations,
      final int defaultSimulationSeed,
      final double simulationTimeHorizon,
      final double[] rho,
      final double[] beta,
      final double[] defaultProbability) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(obligorUniverse, "Obligor universe");
    ArgumentChecker.notNull(recoveryRateModels, "Recovery rate models");

    ArgumentChecker.isTrue(recoveryRateModels.length == obligorUniverse.getNumberOfObligors(), "The number of obligors must equal the number of input recovery rate models");
    ArgumentChecker.isTrue(rho.length == obligorUniverse.getNumberOfObligors(), "The number of obligors must equal the number of input correlations");
    ArgumentChecker.isTrue(beta.length == obligorUniverse.getNumberOfObligors(), "The number of obligors must equal the number of input correlations");
    ArgumentChecker.isTrue(defaultProbability.length == obligorUniverse.getNumberOfObligors(), "The number of obligors must equal the number of input default probabilities");

    ArgumentChecker.notNegative(numberOfSimulations, "Number of simulations");
    ArgumentChecker.notNegative(simulationTimeHorizon, "Simulation time horizon");

    for (int i = 0; i < obligorUniverse.getNumberOfObligors(); i++) {
      ArgumentChecker.isInRangeInclusive(-1.0, 1.0, rho[i]);
      ArgumentChecker.isInRangeInclusive(-1.0, 1.0, beta[i]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _obligorUniverse = obligorUniverse;
    _recoveryRateModels = recoveryRateModels;

    _numberOfSimulations = numberOfSimulations;
    _defaultSimulationSeed = defaultSimulationSeed;

    _simulationTimeHorizon = simulationTimeHorizon;

    _rho = rho;
    _beta = beta;
    _defaultProbability = defaultProbability;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double[][] generateRecoveryRateScenarios(final ScenarioGenerator scenarioGenerator, final int[][] simulatedDefaultScenarios) {

    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    double[][] simulatedRecoveryRateScenarios = new double[numberOfSimulations][numberOfObligors];

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {
      for (int i = 0; i < numberOfObligors; i++) {
        if (simulatedRecoveryRateScenarios[alpha][i] == 1) {

        }

      }
    }

    return simulatedRecoveryRateScenarios;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public int[][] generateDefaultScenarios(final ScenarioGenerator scenarioGenerator) {

    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    NormalDistribution normDist = new NormalDistribution(0.0, 1.0);

    double[] defaultBarrierLevel = new double[numberOfObligors];

    int[][] simulatedDefaultScenarios = new int[numberOfSimulations][numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      final double defaultProbability = scenarioGenerator.getDefaultProbability()[i];
      defaultBarrierLevel[i] = normDist.getInverseCDF(defaultProbability);
    }

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      final double[] systemicFactor = normRand.getVector(1);
      final double[] epsilon = normRand.getVector(numberOfObligors);

      for (int i = 0; i < numberOfObligors; i++) {

        final double rho = scenarioGenerator.getRho()[i];
        final double defaultLatentVariable = rho * systemicFactor[0] + Math.sqrt(1 - rho * rho) * epsilon[i];

        if (defaultLatentVariable < defaultBarrierLevel[i]) {
          simulatedDefaultScenarios[alpha][i] = 1;
        } else {
          simulatedDefaultScenarios[alpha][i] = 0;
        }
      }
    }

    return simulatedDefaultScenarios;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public int[] getNumberOfDefaultsPerScenario(final ScenarioGenerator scenarioGenerator, final int[][] simulatedDefaultScenarios) {

    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    int[] numberOfDefaultsPerScenario = new int[numberOfSimulations];

    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      int numberOfDefaults = 0;

      for (int i = 0; i < numberOfObligors; i++) {
        if (simulatedDefaultScenarios[alpha][i] == 1) {
          numberOfDefaults++;
        }
      }

      numberOfDefaultsPerScenario[alpha] = numberOfDefaults;
    }
    return numberOfDefaultsPerScenario;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public UnderlyingPool getObligorUniverse() {
    return _obligorUniverse;
  }

  public RecoveryRateModel[] getRecoveryRateModels() {
    return _recoveryRateModels;
  }

  public int getNumberofSimulations() {
    return _numberOfSimulations;
  }

  public int getDefaultSimulationSeed() {
    return _defaultSimulationSeed;
  }

  public double getSimulationTimeHorizon() {
    return _simulationTimeHorizon;
  }

  public double[] getRho() {
    return _rho;
  }

  public double[] getBeta() {
    return _beta;
  }

  public double[] getDefaultProbability() {
    return _defaultProbability;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
