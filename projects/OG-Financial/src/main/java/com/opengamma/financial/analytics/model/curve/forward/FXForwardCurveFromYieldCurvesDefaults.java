/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public abstract class FXForwardCurveFromYieldCurvesDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurveFromYieldCurvesDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.FORWARD_CURVE,
      ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
      ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
      ValueRequirementNames.PURE_VOLATILITY_SURFACE,
      ValueRequirementNames.FORWARD_DELTA,
      ValueRequirementNames.DUAL_DELTA,
      ValueRequirementNames.DUAL_GAMMA,
      ValueRequirementNames.FORWARD_GAMMA,
      ValueRequirementNames.FOREX_DOMESTIC_PRICE,
      ValueRequirementNames.FOREX_PV_QUOTES,
      ValueRequirementNames.FORWARD_VEGA,
      ValueRequirementNames.FORWARD_VOMMA,
      ValueRequirementNames.FORWARD_VANNA,
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FX_PRESENT_VALUE,
      ValueRequirementNames.IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_DUAL_DELTA,
      ValueRequirementNames.GRID_DUAL_GAMMA,
      ValueRequirementNames.GRID_FORWARD_DELTA,
      ValueRequirementNames.GRID_FORWARD_GAMMA,
      ValueRequirementNames.GRID_FORWARD_VEGA,
      ValueRequirementNames.GRID_FORWARD_VANNA,
      ValueRequirementNames.GRID_FORWARD_VOMMA,
      ValueRequirementNames.GRID_IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_PRESENT_VALUE
  };
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  //TODO there are ordering issues in this class (the currency pair) - it makes an assumption about which is pay and which is receive
  public FXForwardCurveFromYieldCurvesDefaults(final ComputationTargetType target, final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(target, true);
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "currency and curve config names");
    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      _currencyCurveConfigAndDiscountingCurveNames.put(currencyCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.PAY_CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String firstCurrency = getFirstCurrency(target);
    final String secondCurrency = getSecondCurrency(target);
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(firstCurrency)) {
      s_logger.error("Could not get config for currency " + firstCurrency + "; should never happen");
      return null;
    }
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(secondCurrency)) {
      s_logger.error("Could not get config for currency " + secondCurrency + "; should never happen");
      return null;
    }
    final Pair<String, String> payPair = _currencyCurveConfigAndDiscountingCurveNames.get(firstCurrency);
    final Pair<String, String> receivePair = _currencyCurveConfigAndDiscountingCurveNames.get(secondCurrency);
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
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

  protected Collection<String> getTargets() {
    return _currencyCurveConfigAndDiscountingCurveNames.keySet();
  }

  protected abstract String getFirstCurrency(ComputationTarget target);

  protected abstract String getSecondCurrency(ComputationTarget target);

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CURVE_DEFAULTS;
  }

}
