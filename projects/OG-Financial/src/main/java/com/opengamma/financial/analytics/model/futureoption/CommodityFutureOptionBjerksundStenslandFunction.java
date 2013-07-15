/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;

/**
 *
 */
public abstract class CommodityFutureOptionBjerksundStenslandFunction extends CommodityFutureOptionFunction {

  /**
   * @param valueRequirementName The value requirement name
   */
  public CommodityFutureOptionBjerksundStenslandFunction(final String... valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BJERKSUND_STENSLAND_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }

}
