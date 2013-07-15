/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FXDigitalCallSpreadBlackDeltaPnLDefaults extends DefaultPropertyFunction {
  private final PriorityClass _priority;
  private final String _callSpread;

  public FXDigitalCallSpreadBlackDeltaPnLDefaults(final String priority, final String callSpread) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(callSpread, "call spread");
    _priority = PriorityClass.valueOf(priority);
    _callSpread = callSpread;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    return security instanceof FXDigitalOptionSecurity || security instanceof NonDeliverableFXDigitalOptionSecurity;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE.equals(propertyName)) {
      return Collections.singleton(_callSpread);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }
}
