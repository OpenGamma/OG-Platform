/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.SecurityExerciseTypeVisitor;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 *
 */
public abstract class EquityOptionBAWFunction extends EquityOptionFunction {

  /**
   * @param valueRequirementNames The value requirement names
   */
  public EquityOptionBAWFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof EquityIndexOptionSecurity || security instanceof EquityOptionSecurity)) {
      return false;
    }
    return ((FinancialSecurity) security).accept(new SecurityExerciseTypeVisitor()) instanceof AmericanExerciseType;
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BAW_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }

  public PriorityClass getPriority() {
    return PriorityClass.ABOVE_NORMAL;
  }
}
