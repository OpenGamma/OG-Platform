/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

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

/**
 * Adds {@link ValuePropertyNames#SURFACE} and {@link ValuePropertyNames#CURVE_CALCULATION_CONFIG} to the available
 * {@link ValueRequirement}'s produced by {@link InterestRateFutureOptionBlackFunction}
 * @deprecated The functions for which these defaults apply are deprecated. See {@link InterestRateFutureOptionBlackFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackDefaults.class);
  private static final String[] s_valueRequirements = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.DELTA,
    ValueRequirementNames.GAMMA,
    ValueRequirementNames.VEGA,
    ValueRequirementNames.THETA,
    ValueRequirementNames.POSITION_DELTA,
    ValueRequirementNames.POSITION_GAMMA,
    ValueRequirementNames.POSITION_VEGA,
    ValueRequirementNames.POSITION_THETA,
    ValueRequirementNames.POSITION_RHO,
    ValueRequirementNames.POSITION_WEIGHTED_VEGA,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.SECURITY_MODEL_PRICE,
    ValueRequirementNames.UNDERLYING_MODEL_PRICE,
    ValueRequirementNames.DAILY_PRICE,
    ValueRequirementNames.PNL,
    ValueRequirementNames.FORWARD
  };

  /**
   * This map from currency to curve configuration and surface names
   * may be accessed and set from child classes.
   */
  private HashMap<String, Pair<String, String>> _currencyCurveConfigAndSurfaceNames;

  public InterestRateFutureOptionBlackDefaults(final String... currencyCurveConfigAndSurfaceNames) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(currencyCurveConfigAndSurfaceNames, "currency, curve config and surface names");
    final int nPairs = currencyCurveConfigAndSurfaceNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config name per currency");
    _currencyCurveConfigAndSurfaceNames = new HashMap<>();
    for (int i = 0; i < currencyCurveConfigAndSurfaceNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndSurfaceNames[i + 1], currencyCurveConfigAndSurfaceNames[i + 2]);
      _currencyCurveConfigAndSurfaceNames.put(currencyCurveConfigAndSurfaceNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getTrade().getSecurity() instanceof IRFutureOptionSecurity)) {
      return false;
    }
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final String currency = irFutureOption.getCurrency().getCode();
    return _currencyCurveConfigAndSurfaceNames.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : s_valueRequirements) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
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
    return OpenGammaFunctionExclusions.FUTURE_OPTION_BLACK;
  }

  protected HashMap<String, Pair<String, String>> getCurrencyCurveConfigAndSurfaceNames() {
    return _currencyCurveConfigAndSurfaceNames;
  }

  protected void setCurrencyCurveConfigAndSurfaceNames(final HashMap<String, Pair<String, String>> currencyCurveConfigAndSurfaceNames) {
    _currencyCurveConfigAndSurfaceNames = currencyCurveConfigAndSurfaceNames;
  }

  public static Logger getsLogger() {
    return s_logger;
  }

  public static String[] getsValuerequirements() {
    return s_valueRequirements;
  }

}
