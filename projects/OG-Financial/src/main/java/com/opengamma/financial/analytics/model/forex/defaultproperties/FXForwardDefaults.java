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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXForwardDefaults extends DefaultPropertyFunction {
  
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES
  };
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  public FXForwardDefaults(final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY), true);
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
    final FinancialSecurity fxSecurity = (FinancialSecurity) target.getSecurity();
    final String payCurrency = fxSecurity.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = fxSecurity.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    return _currencyCurveConfigAndDiscountingCurveNames.containsKey(payCurrency) && _currencyCurveConfigAndDiscountingCurveNames.containsKey(receiveCurrency);
  }

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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
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
    return OpenGammaFunctionExclusions.FX_FORWARD_DEFAULTS;
  }

}
