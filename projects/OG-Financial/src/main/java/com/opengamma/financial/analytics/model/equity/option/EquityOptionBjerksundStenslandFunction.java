/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.SecurityExerciseTypeVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;

/**
 * Base class for the {@link CalculationPropertyNamesAndValues#BJERKSUND_STENSLAND_METHOD}<p>
 * This form requires a {@link ValueRequirementNames#BLACK_VOLATILITY_SURFACE}. 
 * See {@link ListedEquityOptionBjerksundStenslandFunction} for a simpler version that instead requires a market price. 
 */
public abstract class EquityOptionBjerksundStenslandFunction extends EquityOptionFunction {

  /**
   * @param valueRequirementNames The value requirement names
   */
  public EquityOptionBjerksundStenslandFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    return ((FinancialSecurity) security).accept(new SecurityExerciseTypeVisitor()) instanceof AmericanExerciseType;
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
