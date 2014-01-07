/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXForwardPnLDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPnLDefaults.class);
  private final String _start;
  private final String _end;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  public FXForwardPnLDefaults(final String start, final String end, final String scheduleCalculator,
      final String samplingFunction, final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    _start = start;
    _end = end;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "currency and curve config names");
    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      _currencyCurveConfigAndDiscountingCurveNames.put(currencyCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    if (!(security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity)) {
      return false;
    }
    final FinancialSecurity fxSecurity = (FinancialSecurity) security;
    final String payCurrency = fxSecurity.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = fxSecurity.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    return _currencyCurveConfigAndDiscountingCurveNames.containsKey(payCurrency) && _currencyCurveConfigAndDiscountingCurveNames.containsKey(receiveCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String requirementName : new String[] {ValueRequirementNames.PNL_SERIES, ValueRequirementNames.YIELD_CURVE_PNL_SERIES, ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES}) {
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.PAY_CURVE);
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(requirementName, HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
      defaults.addValuePropertyName(requirementName, HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.SCHEDULE_CALCULATOR);
      defaults.addValuePropertyName(requirementName, ValuePropertyNames.SAMPLING_FUNCTION);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY.equals(propertyName)) {
      return Collections.singleton(_start);
    }
    if (HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY.equals(propertyName)) {
      return Collections.singleton(_end);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final String payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(payCurrency)) {
      s_logger.error("Could not get config for pay currency " + payCurrency + "; should never happen");
      return null;
    }
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(receiveCurrency)) {
      s_logger.error("Could not get config for receive currency " + receiveCurrency + "; should never happen");
      return null;
    }
    final Pair<String, String> payPair = _currencyCurveConfigAndDiscountingCurveNames.get(payCurrency);
    final Pair<String, String> receivePair = _currencyCurveConfigAndDiscountingCurveNames.get(receiveCurrency);
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(payPair.getSecond());
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(receivePair.getSecond());
    }
    if (ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(payPair.getFirst());
    }
    if (ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(receivePair.getFirst());
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }

}
