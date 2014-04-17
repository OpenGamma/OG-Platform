/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class InterestRateFutureOptionBlackYieldCurveNodePnLDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackYieldCurveNodePnLDefaults.class);
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final HashMap<String, Pair<String, String>> _currencyCurveConfigAndSurfaceNames;

  public InterestRateFutureOptionBlackYieldCurveNodePnLDefaults(final String samplingPeriod, final String scheduleCalculator, final String samplingFunction,
      final String... currencyCurveConfigAndSurfaceNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(currencyCurveConfigAndSurfaceNames, "currency, curve config and surface names");
    final int nPairs = currencyCurveConfigAndSurfaceNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config name and surface name per currency");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _currencyCurveConfigAndSurfaceNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndSurfaceNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndSurfaceNames[i + 1], currencyCurveConfigAndSurfaceNames[i + 2]);
      _currencyCurveConfigAndSurfaceNames.put(currencyCurveConfigAndSurfaceNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPositionOrTrade().getSecurity() instanceof IRFutureOptionSecurity)) {
      return false;
    }
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) target.getPositionOrTrade().getSecurity();
    final String currency = irFutureOption.getCurrency().getCode();
    return _currencyCurveConfigAndSurfaceNames.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity()).getCode();
    if (!_currencyCurveConfigAndSurfaceNames.containsKey(currencyName)) {
      s_logger.error("Could not config and surface names for currency " + currencyName + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _currencyCurveConfigAndSurfaceNames.get(currencyName);
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }

}
