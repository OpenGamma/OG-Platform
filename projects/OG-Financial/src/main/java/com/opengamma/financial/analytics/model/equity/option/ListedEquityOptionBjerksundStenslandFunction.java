/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.SecurityExerciseTypeVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;

/**
* Base class for the {@link CalculationPropertyNamesAndValues#BJERKSUND_STENSLAND_METHOD}.<p>
* This form requires that a market price is available for the option.
* When this isn't available, pricing can be done via {@link ValueRequirementNames#BLACK_VOLATILITY_SURFACE}. 
* See {@link EquityOptionBjerksundStenslandFunction} 
*/
public abstract class ListedEquityOptionBjerksundStenslandFunction extends ListedEquityOptionFunction {

  /**
   * @param valueRequirementNames The value requirement names
   */
  public ListedEquityOptionBjerksundStenslandFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    return ((FinancialSecurity) security).accept(new SecurityExerciseTypeVisitor()) instanceof AmericanExerciseType;
  }
  
  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BJERKSUND_STENSLAND_LISTED_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }

}
