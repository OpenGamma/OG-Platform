/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

/**
 * Class to define the characteristics of variation margin required by a CCP for a centrally cleared trade
 */
public class VariationMarginDefinition extends CollateralDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public VariationMarginDefinition(
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
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
