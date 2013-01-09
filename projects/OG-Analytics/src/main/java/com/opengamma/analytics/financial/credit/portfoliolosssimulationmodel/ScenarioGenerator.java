/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ScenarioGenerator {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Do we need to do any arg checks on the simulation seed?

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final Obligor[] _obligorUniverse;

  private final RecoveryRateModel[] _recoveryRateModels;

  private final int _numberOfSimulations;

  private final int _simulationSeed;

  private final double _simulationTimeHorizon;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ScenarioGenerator(
      final Obligor[] obligorUniverse,
      final RecoveryRateModel[] recoveryRateModels,
      final int numberOfSimulations,
      final int simulationSeed,
      final double simulationTimeHorizon) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(obligorUniverse, "Obligors");
    ArgumentChecker.notNull(recoveryRateModels, "Recovery rate models");

    ArgumentChecker.notNegative(numberOfSimulations, "Number of simulations");
    ArgumentChecker.notNegative(simulationTimeHorizon, "Simulation time horizon");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _obligorUniverse = obligorUniverse;
    _recoveryRateModels = recoveryRateModels;

    _numberOfSimulations = numberOfSimulations;
    _simulationSeed = simulationSeed;

    _simulationTimeHorizon = simulationTimeHorizon;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public Obligor[] getObligorUniverse() {
    return _obligorUniverse;
  }

  public RecoveryRateModel[] getRecoveryRateModels() {
    return _recoveryRateModels;
  }

  public int getNumberofSimulations() {
    return _numberOfSimulations;
  }

  public int getSimulationSeed() {
    return _simulationSeed;
  }

  public double getSimulationTimeHorizon() {
    return _simulationTimeHorizon;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
