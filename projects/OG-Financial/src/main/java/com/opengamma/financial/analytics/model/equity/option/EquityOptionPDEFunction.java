/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;

/**
 *
 */
public abstract class EquityOptionPDEFunction extends EquityOptionFunction {

  public EquityOptionPDEFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.PDE;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.PDE;
  }

}
