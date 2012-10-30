/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaults;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class EquityFuturesDefaultPropertiesFunction extends DefaultPropertyFunction {

  private final Map<Currency, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;
  private final String _pricingMethodName;
  private final PriorityClass _priority;


  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES
  };


  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other. ABOVE_NORMAL, NORMAL, BELOW_NORMAL, LOWEST
   * @param pricingMethodName One of MARK_TO_MARKET, COST_OF_CARRY or DIVIDEND_YIELD
   * @param currencyCurveConfigAndDiscountingCurveNames Choice of MultiCurveCalculationConfig. e.g. DefaultTwoCurveUSDConfig
   */
  public EquityFuturesDefaultPropertiesFunction(String priority, final String pricingMethodName, final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.TRADE, true);
    Validate.notNull(priority, "No priority was provided.");
    Validate.notNull(pricingMethodName, "No pricingMethodName was provided to use as default value.");
    Validate.notNull(currencyCurveConfigAndDiscountingCurveNames, "No curveCalculationConfigName was provided to use as default value.");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<Currency, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      final Currency ccy = Currency.of(currencyCurveConfigAndDiscountingCurveNames[i]);
      _currencyCurveConfigAndDiscountingCurveNames.put(ccy, pair);
    }

    _pricingMethodName = pricingMethodName;
    Validate.isTrue(pricingMethodName.equals(EquityFuturePricerFactory.MARK_TO_MARKET)
        || pricingMethodName.equals(EquityFuturePricerFactory.COST_OF_CARRY)
        || pricingMethodName.equals(EquityFuturePricerFactory.DIVIDEND_YIELD),
        "OG-Analytics provides the following pricing methods for EquityFutureSecurity: MARK_TO_MARKET, DIVIDEND_YIELD and COST_OF_CARRY. " +
      "If specifying a default, it must be one of these three.");
  }

  @Override
  protected void getDefaults(PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    final EquityFutureSecurity eqSec = (EquityFutureSecurity) target.getTrade().getSecurity();
    final Currency ccy = eqSec.getCurrency();
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(ccy)) {
      s_logger.error("Could not get config for currency " + ccy + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _currencyCurveConfigAndDiscountingCurveNames.get(ccy);

    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    } else if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    } else if (ValuePropertyNames.CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_pricingMethodName);
    }
    return null;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    if (!(target.getTrade().getSecurity() instanceof EquityFutureSecurity)) {
      return false;
    }
    final EquityFutureSecurity eqSec = (EquityFutureSecurity) target.getTrade().getSecurity();
    final Currency ccy = eqSec.getCurrency();
    final boolean applies = _currencyCurveConfigAndDiscountingCurveNames.containsKey(ccy);
    return applies;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_FUTURE_DEFAULTS;
  }

  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardDefaults.class);
}
