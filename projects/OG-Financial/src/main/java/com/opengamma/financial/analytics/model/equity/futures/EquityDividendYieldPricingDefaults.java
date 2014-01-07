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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class EquityDividendYieldPricingDefaults extends DefaultPropertyFunction {
  /** The default values */
  private final Map<Currency, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;
  /** The priority */
  private final PriorityClass _priority;

  /** The value requirements for which these defaults apply */
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
   * @param currencyCurveConfigAndDiscountingCurveNames Choice of MultiCurveCalculationConfig. e.g. DefaultTwoCurveUSDConfig
   */
  public EquityDividendYieldPricingDefaults(final String priority, final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(priority, "No priority was provided.");
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "No curveCalculationConfigName was provided to use as default value.");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<Currency, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      final Currency ccy = Currency.of(currencyCurveConfigAndDiscountingCurveNames[i]);
      _currencyCurveConfigAndDiscountingCurveNames.put(ccy, pair);
    }
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(ccy)) {
      s_logger.error("Could not get config for currency " + ccy + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _currencyCurveConfigAndDiscountingCurveNames.get(ccy);

    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    } else if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security sec = target.getTrade().getSecurity();
    if (!(sec instanceof EquityFutureSecurity || sec instanceof EquityIndexDividendFutureSecurity || sec instanceof IndexFutureSecurity)) {
      return false;
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(sec);
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

  private static final Logger s_logger = LoggerFactory.getLogger(EquityDividendYieldPricingDefaults.class);
}
