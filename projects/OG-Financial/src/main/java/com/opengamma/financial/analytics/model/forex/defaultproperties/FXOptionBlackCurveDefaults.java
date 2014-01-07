/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Default properties for FX options priced using the Black functions.
 * @deprecated These defaults are used by deprecated functions.
 */
@Deprecated
public class FXOptionBlackCurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackCurveDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_GAMMA_P,
    ValueRequirementNames.VEGA_MATRIX,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.VALUE_PHI,
    ValueRequirementNames.VALUE_VOMMA,
    ValueRequirementNames.VALUE_VANNA,
    ValueRequirementNames.DELTA,
    ValueRequirementNames.FORWARD_DELTA,
    ValueRequirementNames.GAMMA,
    ValueRequirementNames.FORWARD_GAMMA,
    ValueRequirementNames.FORWARD_VEGA,
    ValueRequirementNames.FORWARD_DRIFTLESS_THETA,
    ValueRequirementNames.THETA
  };
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  /**
   * @param currencyCurveConfigAndDiscountingCurveNames Values for the properties per currency: an array of strings where the <i>i<sup>th</sup></i> currency has properties:
   *          <ul>
   *          <li><i>i</i> = currency name,
   *          <li><i>i + 1</i> = curve configuration name
   *          <li><i>i + 2</i> = discounting curve name
   *          </ul>
   */
  public FXOptionBlackCurveDefaults(final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY.or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY).or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_OPTION_SECURITY).or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY), true);
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "currency and curve config names");
    ArgumentChecker.isTrue(currencyCurveConfigAndDiscountingCurveNames.length % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      _currencyCurveConfigAndDiscountingCurveNames.put(currencyCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    return (_currencyCurveConfigAndDiscountingCurveNames.containsKey(putCurrency) && _currencyCurveConfigAndDiscountingCurveNames.containsKey(callCurrency));
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.PUT_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.CALL_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(putCurrency)) {
      s_logger.error("Could not get config for put currency " + putCurrency + "; should never happen");
      return null;
    }
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(callCurrency)) {
      s_logger.error("Could not get config for call currency " + callCurrency + "; should never happen");
      return null;
    }
    final String putCurveConfig, callCurveConfig, putCurve, callCurve;
    final Pair<String, String> firstCurrencyValues = _currencyCurveConfigAndDiscountingCurveNames.get(putCurrency);
    putCurveConfig = firstCurrencyValues.getFirst();
    putCurve = firstCurrencyValues.getSecond();
    final Pair<String, String> secondCurrencyValues = _currencyCurveConfigAndDiscountingCurveNames.get(callCurrency);
    callCurveConfig = secondCurrencyValues.getFirst();
    callCurve = secondCurrencyValues.getSecond();
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
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CURVE_DEFAULTS;
  }

  protected static String[] getRequirementNames() {
    return VALUE_REQUIREMENTS;
  }

}
