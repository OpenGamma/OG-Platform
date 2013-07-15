/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModelConstant;

/**
 * Class to provide utility methods for the portfolio loss simulation model
 */
public class SimulationMethods {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Eventually want to put all these into a more meaningful location

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public SimulationMethods() {

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public RecoveryRateModel[] constructRecoveryRateModels(final int numberOfObligors) {

    RecoveryRateModel[] recoveryRateModels = new RecoveryRateModel[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {

      final RecoveryRateModel recRateModel = new RecoveryRateModelConstant(0.4);

      recoveryRateModels[i] = recRateModel;
    }

    return recoveryRateModels;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double[] constructCorrelationVector(final int numberOfObligors, final double homogeneousCorrelation) {

    double[] correlationVector = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      correlationVector[i] = homogeneousCorrelation;
    }

    return correlationVector;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double[] constructDefaultProbabilityVector(final int numberOfObligors, final double homogeneousDefaultProbability) {

    double[] defaultProbabilityVector = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      defaultProbabilityVector[i] = homogeneousDefaultProbability;
    }

    return defaultProbabilityVector;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
