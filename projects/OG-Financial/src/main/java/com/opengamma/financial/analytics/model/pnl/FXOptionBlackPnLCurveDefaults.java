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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXOptionBlackPnLCurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackPnLCurveDefaults.class);
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  public FXOptionBlackPnLCurveDefaults(final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "property values by currency");
    ArgumentChecker.isTrue(currencyCurveConfigAndDiscountingCurveNames.length % 3 == 0, "Must have a curve calculation configuration name and curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      _currencyCurveConfigAndDiscountingCurveNames.put(currencyCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final boolean isFXOption = (security instanceof FXOptionSecurity
        || security instanceof FXBarrierOptionSecurity
        || security instanceof FXDigitalOptionSecurity
        || security instanceof NonDeliverableFXOptionSecurity
        || security instanceof NonDeliverableFXDigitalOptionSecurity);
    if (!isFXOption) {
      return false;
    }
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    return (_currencyCurveConfigAndDiscountingCurveNames.containsKey(putCurrency) && _currencyCurveConfigAndDiscountingCurveNames.containsKey(callCurrency));
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.PUT_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.CALL_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
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

}
