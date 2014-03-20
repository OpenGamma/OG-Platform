/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;

import com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the clean price of a bond from yield curves.
 */
public class BondCleanPriceFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#CLEAN_PRICE} and
   * the calculator to {@link CleanPriceFromCurvesCalculator}.
   */
  public BondCleanPriceFromCurvesFunction() {
    super(CLEAN_PRICE, CleanPriceFromCurvesCalculator.getInstance());
  }

}
