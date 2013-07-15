/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to define the characteristics of initial margin required by a CCP for a centrally cleared trade
 */
public class InitialMarginDefinition extends CollateralDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The confidence level to which the initial margin should be computed
  private final double _confidenceLevel;

  // The liquidity horizon (in days) over which it will take to liquidate the position
  private final int _liquidityHorizon;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public InitialMarginDefinition(
      final double collateralAmount,
      final double independentAmount,
      final double minimumTransferAmount,
      final double collateralTriggerThreshold,
      final double confidenceLevel,
      final int liquidityHorizon,
      final MarginCallFrequency minimumMarginCallGrequency) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    super(collateralAmount, independentAmount, minimumTransferAmount, collateralTriggerThreshold, minimumMarginCallGrequency);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.isInRangeInclusive(0.0, 1.0, confidenceLevel);
    ArgumentChecker.notNegative(liquidityHorizon, "Liquidity Horizon");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _confidenceLevel = confidenceLevel;

    _liquidityHorizon = liquidityHorizon;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getConfidenceLevel() {
    return _confidenceLevel;
  }

  public int getLiquidityHorizon() {
    return _liquidityHorizon;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
