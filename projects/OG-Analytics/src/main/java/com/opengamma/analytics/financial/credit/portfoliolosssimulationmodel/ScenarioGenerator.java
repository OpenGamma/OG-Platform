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
  // TODO : Sort out the simulation seeds to make sure they are correctly used
  // TODO : Sort out the public/private access

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The universe of obligors to simulate scenarios for (treat as a very large 'UnderlyingPool' object)
  private final UnderlyingPool _obligorUniverse;

  // The recovery rate models (constant or stochastic) for each obligor in the universe of obligors
  private final RecoveryRateModel[] _recoveryRateModels;

  // The number of simulations
  private final int _numberOfSimulations;

  // The default simulation random number seed
  private final int _defaultSimulationSeed;

  // The simulation seed for sampling stochastic recoveries
  private final int _recoveryRateSimulationSeed;

  // The time horizon for the simulation (usually an integer, but need not be)
  private final double _simulationTimeHorizon;

  // The correlation of the defaults with the systemic factor
  private final double[] _rho;

  // The correlation of the sampled recovery rates with the systemic factor
  private final double[] _beta;

  // The default probability of the obligors
  private final double[] _defaultProbability;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ScenarioGenerator(
      final UnderlyingPool obligorUniverse,
      final RecoveryRateModel[] recoveryRateModels,
      final int numberOfSimulations,
      final int defaultSimulationSeed,
      final int recoveryRateSimulationSeed,
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
    _recoveryRateSimulationSeed = recoveryRateSimulationSeed;

    _simulationTimeHorizon = simulationTimeHorizon;

    _rho = rho;
    _beta = beta;
    _defaultProbability = defaultProbability;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to compute the recovery rates for each obligor for each simulated scenario
  public double[][] generateRecoveryRateScenarios(final ScenarioGenerator scenarioGenerator, final SimulatedObligorDefaultState[][] simulatedDefaultScenarios) {

    // Determine the number of simulations and number of obligors in the universe
    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    // The matrix of simulated recovery rates per simulation to output
    double[][] simulatedRecoveryRateScenarios = new double[numberOfSimulations][numberOfObligors];

    // Construct a N(0, 1) random number generator
    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    // Construct a N(0, 1) distribution object (for calculating the default barrier level)
    NormalDistribution normDist = new NormalDistribution(0.0, 1.0);

    // Compute the vector of systemic factors
    final double[] systemicFactor = normRand.getVector(numberOfSimulations);

    // Main loop of simulation
    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      // Loop over each of the obligors
      for (int i = 0; i < numberOfObligors; i++) {

        // Did obligor i default on scenario alpha ...
        if (simulatedDefaultScenarios[alpha][i] == SimulatedObligorDefaultState.DEFAULTED) {

          // ... yes, then calculate the stochastic recovery rate

          // Calculate an idiosyncratic N(0, 1) deviate
          final double[] eta = normRand.getVector(1);

          // Get the correlation (coupling) of the sampled recovery rate with the systemic factor
          final double beta = scenarioGenerator.getBeta()[i];

          // Compute the recovery rate latent variable for this obligor for this simulation
          final double recoveryRateLatentVariable = beta * systemicFactor[i] + Math.sqrt(1 - beta * beta) * eta[0];

          // Calculate the recovery rate

          // FIXME : 
          simulatedRecoveryRateScenarios[alpha][i] = 0.0;

        } else {

          // ... no, in which case the recovery rate is just the user input recovery rate

          simulatedRecoveryRateScenarios[alpha][i] = scenarioGenerator.getRecoveryRateModels()[i].getRecoveryRate();
        }

      }
    }

    return simulatedRecoveryRateScenarios;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to generate a vector of N(0, 1) deviates corresponding to the systemic factor for each simulation
  private double[] generateSystemicFactors(final int numberOfSimulations, final int defaultSimulationSeed) {

    // Construct a N(0, 1) random number generator
    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    // Compute the vector of systemic factors
    final double[] systemicFactor = normRand.getVector(numberOfSimulations);

    return systemicFactor;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to compute the default barrier levels for the obligors
  private double[] generateDefaultBarrierLevels(ScenarioGenerator scenarioGenerator) {

    final int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    // Vector to hold the default barrier level for each obligor
    double[] defaultBarrierLevel = new double[numberOfObligors];

    // Construct a N(0, 1) distribution object (for calculating the default barrier level)
    NormalDistribution normDist = new NormalDistribution(0.0, 1.0);

    // Compute the default barrier level for each obligor
    for (int i = 0; i < numberOfObligors; i++) {

      final double defaultProbability = scenarioGenerator.getDefaultProbability()[i];
      defaultBarrierLevel[i] = normDist.getInverseCDF(defaultProbability);
    }

    return defaultBarrierLevel;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to compute the simulated defaults for each simulation
  public SimulatedObligorDefaultState[][] generateDefaultScenarios(final ScenarioGenerator scenarioGenerator) {

    // Determine the number of simulations and number of obligors in the universe
    int numberOfSimulations = scenarioGenerator.getNumberofSimulations();
    int numberOfObligors = scenarioGenerator.getObligorUniverse().getNumberOfObligors();

    // Construct a N(0, 1) random number generator
    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    // The matrix of simulated defaults per simulation to output
    SimulatedObligorDefaultState[][] simulatedDefaultScenarios = new SimulatedObligorDefaultState[numberOfSimulations][numberOfObligors];

    // Compute the default barrier level for each obligor
    final double[] defaultBarrierLevel = generateDefaultBarrierLevels(scenarioGenerator);

    // Compute the vector of systemic factors (one sample for each simulation)
    final double[] systemicFactor = generateSystemicFactors(numberOfSimulations, scenarioGenerator.getDefaultSimulationSeed());

    // Main loop of simulation
    for (int alpha = 0; alpha < numberOfSimulations; alpha++) {

      // Construct a vector of idiosyncratic N(0, 1) deviates for this simulation
      final double[] epsilon = normRand.getVector(numberOfObligors);

      // Loop over each of the obligors
      for (int i = 0; i < numberOfObligors; i++) {

        // Get the correlation (coupling) of the default of this obligor with the systemic factor
        final double rho = scenarioGenerator.getRho()[i];

        // Compute the default latent variable for this obligor for this simulation
        final double defaultLatentVariable = rho * systemicFactor[0] + Math.sqrt(1 - rho * rho) * epsilon[i];

        // Did the obligor i default in simulation alpha ... 
        if (defaultLatentVariable < defaultBarrierLevel[i]) {
          // ... yes
          simulatedDefaultScenarios[alpha][i] = SimulatedObligorDefaultState.DEFAULTED;
        } else {
          // ... no
          simulatedDefaultScenarios[alpha][i] = SimulatedObligorDefaultState.NOTDEFAULTED;
        }

      }
    }

    return simulatedDefaultScenarios;
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

  public int getRecoveryRateSimulationSeed() {
    return _recoveryRateSimulationSeed;
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
