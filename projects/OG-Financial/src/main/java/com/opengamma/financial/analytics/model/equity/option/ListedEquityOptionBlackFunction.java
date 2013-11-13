/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;

/**
* In this form, we do not take as input an entire volatility surface {@link ValueRequirementNames#BLACK_VOLATILITY_SURFACE}.
* Instead, the implied volatility is implied by the market_value of the security, along with it's contract parameters of expiry and strike,
* along with the requirement of a forward curve (ValueRequirementNames.FORWARD_CURVE). 
*/
public abstract class ListedEquityOptionBlackFunction extends ListedEquityOptionFunction {

  /** @param valueRequirementName The value requirement names, not null */
  public ListedEquityOptionBlackFunction(final String... valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BLACK_LISTED_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }
  
  
}
