/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class InterestRateFutureYieldCurveNodePnLDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureYieldCurveNodePnLDefaults.class);
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final Map<String, String> _currencyAndCurveConfigNames;

  public InterestRateFutureYieldCurveNodePnLDefaults(final String samplingPeriod, final String scheduleCalculator, final String samplingFunction,
      final String... currencyAndCurveConfigNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    final int nPairs = currencyAndCurveConfigNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve config name per currency");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _currencyAndCurveConfigNames = new HashMap<String, String>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 2) {
      _currencyAndCurveConfigNames.put(currencyAndCurveConfigNames[i], currencyAndCurveConfigNames[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPositionOrTrade().getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      return false;
    }
    if (!(security instanceof InterestRateFutureSecurity)) {
      return false;
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
    return _currencyAndCurveConfigNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      final String currencyName = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity()).getCode();
      final String configName = _currencyAndCurveConfigNames.get(currencyName);
      if (configName == null) {
        s_logger.error("Could not get config for currency " + currencyName + "; should never happen");
        return null;
      }
      return Collections.singleton(configName);
    }
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }

}
