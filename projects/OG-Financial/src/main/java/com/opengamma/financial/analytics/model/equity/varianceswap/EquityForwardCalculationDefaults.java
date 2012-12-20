/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class EquityForwardCalculationDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCalculationDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD
  };
  private final PriorityClass _priority;
  private final Map<String, Pair<String, String>> _equityCurveConfigAndDiscountingCurveNames;

  public EquityForwardCalculationDefaults(final String priority, final String... equityCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(equityCurveConfigAndDiscountingCurveNames, "equity and curve config names");
    final int nPairs = equityCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per equity");
    _priority = PriorityClass.valueOf(priority);
    _equityCurveConfigAndDiscountingCurveNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < equityCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(equityCurveConfigAndDiscountingCurveNames[i + 1], equityCurveConfigAndDiscountingCurveNames[i + 2]);
      _equityCurveConfigAndDiscountingCurveNames.put(equityCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      return false;
    }
    if (!(security instanceof EquityVarianceSwapSecurity)) {
      return false;
    }
    final EquityVarianceSwapSecurity varianceSwap = (EquityVarianceSwapSecurity) security;
    final String underlyingEquity = varianceSwap.getSpotUnderlyingId().getValue();
    return _equityCurveConfigAndDiscountingCurveNames.containsKey(underlyingEquity);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final EquityVarianceSwapSecurity varianceSwap = (EquityVarianceSwapSecurity) target.getSecurity();
    final String underlyingEquity = varianceSwap.getSpotUnderlyingId().getValue();
    if (!_equityCurveConfigAndDiscountingCurveNames.containsKey(underlyingEquity)) {
      s_logger.error("Could not get config for equity " + underlyingEquity + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _equityCurveConfigAndDiscountingCurveNames.get(underlyingEquity);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_VARIANCE_SWAP_DEFAULTS;
  }
}
