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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FXOptionBlackDeltaPnLDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackDeltaPnLDefaults.class);
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final PriorityClass _priority;
  private final String _interpolatorName;
  private final String _leftExtrapolatorName;
  private final String _rightExtrapolatorName;
  private final Map<String, Pair<String, String>> _propertyValuesByFirstCurrency;
  private final Map<String, Pair<String, String>> _propertyValuesBySecondCurrency;
  private final Map<Pair<String, String>, String> _surfaceNameByCurrencyPair;

  public FXOptionBlackDeltaPnLDefaults(final String samplingPeriod, final String scheduleCalculator, final String samplingFunction, final String priority,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName, final String... propertyValuesByCurrencies) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(propertyValuesByCurrencies, "property values by currency");
    ArgumentChecker.isTrue(propertyValuesByCurrencies.length % 7 == 0, "Must have two currencies, one curve config and discounting curve name per currency pair and one surface name");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _priority = PriorityClass.valueOf(priority);
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _propertyValuesByFirstCurrency = new HashMap<String, Pair<String, String>>();
    _propertyValuesBySecondCurrency = new HashMap<String, Pair<String, String>>();
    _surfaceNameByCurrencyPair = new HashMap<Pair<String, String>, String>();
    for (int i = 0; i < propertyValuesByCurrencies.length; i += 7) {
      final String firstCurrency = propertyValuesByCurrencies[i];
      final Pair<String, String> firstCurrencyValues = Pair.of(propertyValuesByCurrencies[i + 1], propertyValuesByCurrencies[i + 2]);
      final String secondCurrency = propertyValuesByCurrencies[i + 3];
      ArgumentChecker.isFalse(firstCurrency.equals(secondCurrency), "The two currencies must not be equal; have {} and {}", firstCurrency, secondCurrency);
      final Pair<String, String> secondCurrencyValues = Pair.of(propertyValuesByCurrencies[i + 4], propertyValuesByCurrencies[i + 5]);
      final String surfaceName = propertyValuesByCurrencies[i + 6];
      _propertyValuesByFirstCurrency.put(firstCurrency, firstCurrencyValues);
      _propertyValuesBySecondCurrency.put(secondCurrency, secondCurrencyValues);
      _surfaceNameByCurrencyPair.put(Pair.of(firstCurrency, secondCurrency), surfaceName);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    if (!(target.getPosition().getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    if (!(security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity)) {
      return false;
    }
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    return (_propertyValuesByFirstCurrency.containsKey(putCurrency) && _propertyValuesBySecondCurrency.containsKey(callCurrency)) ||
        (_propertyValuesByFirstCurrency.containsKey(callCurrency) && _propertyValuesBySecondCurrency.containsKey(putCurrency));
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.PUT_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.CALL_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
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
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    if (!(_propertyValuesByFirstCurrency.containsKey(putCurrency) || _propertyValuesBySecondCurrency.containsKey(putCurrency))) {
      s_logger.error("Could not get config for put currency " + putCurrency + "; should never happen");
      return null;
    }
    if (!(_propertyValuesByFirstCurrency.containsKey(callCurrency) || _propertyValuesBySecondCurrency.containsKey(callCurrency))) {
      s_logger.error("Could not get config for call currency " + callCurrency + "; should never happen");
      return null;
    }
    final String putCurveConfig, callCurveConfig, putCurve, callCurve;
    if (_propertyValuesByFirstCurrency.containsKey(putCurrency)) {
      final Pair<String, String> firstCurrencyValues = _propertyValuesByFirstCurrency.get(putCurrency);
      putCurveConfig = firstCurrencyValues.getFirst();
      putCurve = firstCurrencyValues.getSecond();
      final Pair<String, String> secondCurrencyValues = _propertyValuesBySecondCurrency.get(callCurrency);
      callCurveConfig = secondCurrencyValues.getFirst();
      callCurve = secondCurrencyValues.getSecond();
    } else {
      final Pair<String, String> firstCurrencyValues = _propertyValuesByFirstCurrency.get(callCurrency);
      callCurveConfig = firstCurrencyValues.getFirst();
      callCurve = firstCurrencyValues.getSecond();
      final Pair<String, String> secondCurrencyValues = _propertyValuesBySecondCurrency.get(putCurrency);
      putCurveConfig = secondCurrencyValues.getFirst();
      putCurve = secondCurrencyValues.getSecond();
    }
    if (FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(putCurveConfig);
    }
    if (FXOptionBlackFunction.PUT_CURVE.equals(propertyName)) {
      return Collections.singleton(putCurve);
    }
    if (FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(callCurveConfig);
    }
    if (FXOptionBlackFunction.CALL_CURVE.equals(propertyName)) {
      return Collections.singleton(callCurve);
    }
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      Pair<String, String> pair = Pair.of(putCurrency, callCurrency);
      if (_surfaceNameByCurrencyPair.containsKey(pair)) {
        return Collections.singleton(_surfaceNameByCurrencyPair.get(pair));
      }
      pair = Pair.of(callCurrency, putCurrency);
      if (_surfaceNameByCurrencyPair.containsKey(pair)) {
        return Collections.singleton(_surfaceNameByCurrencyPair.get(pair));
      }
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
