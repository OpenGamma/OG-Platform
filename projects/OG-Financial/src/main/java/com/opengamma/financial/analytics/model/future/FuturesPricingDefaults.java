/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Defaults the pricing of Futures to ValuePropertyNames.CALCULATION_METHOD = "MarkToMarket" (CalculationPropertyNamesAndValues.MARK_TO_MARKET_METHOD)
 */
public class FuturesPricingDefaults extends StaticDefaultPropertyFunction {

  /** The priority */
  private final PriorityClass _priority;

  /** The default values */
  private final Set<String> _calculationMethod;

  /** The value requirements for which these defaults apply */
  private static final String[] s_valueNames = new String[] {
      ValueRequirementNames.PRESENT_VALUE,
  };

  public FuturesPricingDefaults(final String priority, final String calculationMethod) {
    super(ComputationTargetType.TRADE, ValuePropertyNames.CALCULATION_METHOD, true, s_valueNames);
    ArgumentChecker.notNull(priority, "No priority was provided.");
    ArgumentChecker.notNull(calculationMethod, "No calculationMethod was provided. Try MarkToMarket");
    _priority = PriorityClass.valueOf(priority);
    _calculationMethod = Collections.singleton(calculationMethod);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return _calculationMethod;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security sec = target.getTrade().getSecurity();
    if (!(sec instanceof EquityFutureSecurity || sec instanceof EquityIndexDividendFutureSecurity || sec instanceof IndexFutureSecurity)) {
      return false;
    }
    return true;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CALCULATION_METHOD_DEFAULTS;
  }

}
