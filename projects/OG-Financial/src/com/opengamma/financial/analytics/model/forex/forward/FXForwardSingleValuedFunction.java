/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class FXForwardSingleValuedFunction extends FXForwardFunction {

  public FXForwardSingleValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = key.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
        break;
      }
    }
    assert currencyPairConfigName != null;
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target, baseQuotePair).get());
    return Collections.singleton(resultSpec);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(PAY_CURVE_CALC_CONFIG)
        .withAny(RECEIVE_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.CURRENCY);
  }

  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final CurrencyPair baseQuotePair) {
    return createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(PAY_CURVE_CALC_CONFIG)
        .withAny(RECEIVE_CURVE_CALC_CONFIG)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target, baseQuotePair));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveCalculationConfig = desiredValue.getConstraint(PAY_CURVE_CALC_CONFIG);
    final String receiveCurveCalculationConfig = desiredValue.getConstraint(RECEIVE_CURVE_CALC_CONFIG);
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(PAY_CURVE_CALC_CONFIG, payCurveCalculationConfig)
        .with(RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.CURRENCY, currency);
  }

  protected static String getResultCurrency(final ComputationTarget target, final CurrencyPair baseQuotePair) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    Currency ccy;
    if (baseQuotePair.getBase().equals(payCurrency)) {
      ccy = payCurrency;
    } else {
      ccy = receiveCurrency;
    }
    return ccy.getCode();
  }
}
