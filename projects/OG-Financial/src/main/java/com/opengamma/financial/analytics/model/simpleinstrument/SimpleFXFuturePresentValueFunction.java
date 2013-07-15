/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFXFutureDataBundle;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFXFuturePresentValueCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.SimpleFutureConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class SimpleFXFuturePresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final SimpleFutureConverter CONVERTER = new SimpleFutureConverter();
  private static final SimpleFXFuturePresentValueCalculator CALCULATOR = new SimpleFXFuturePresentValueCalculator();
  private final String _payCurveName;
  private final String _receiveCurveName;

  public SimpleFXFuturePresentValueFunction(final String payCurveName, final String receiveCurveName) {
    Validate.notNull(payCurveName, "pay curve name");
    Validate.notNull(receiveCurveName, "receive curve name");
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FXFutureSecurity security = (FXFutureSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final Currency payCurrency = security.getNumerator();
    final Object payCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(payCurrency, _payCurveName, null, null));
    if (payCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + _payCurveName + " curve");
    }
    final Currency receiveCurrency = security.getDenominator();
    final Object receiveCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(receiveCurrency, _receiveCurveName, null, null));
    if (receiveCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + _receiveCurveName + " curve");
    }
    // TODO: The convention is only looked up here so that we can convert the spot rate; would be better to request the spot rate using the correct currency pair in the first place
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final Currency currencyBase = currencyPair.getBase();
    final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get market data for spot rate");
    }
    double spot = (Double) spotObject;
    if (!receiveCurrency.equals(currencyBase) && receiveCurrency.equals(security.getCurrency())) {
      spot = 1. / spot;
    }
    final YieldAndDiscountCurve payCurve = (YieldAndDiscountCurve) payCurveObject;
    final YieldAndDiscountCurve receiveCurve = (YieldAndDiscountCurve) receiveCurveObject;
    final SimpleFXFutureDataBundle data = new SimpleFXFutureDataBundle(payCurve, receiveCurve, spot);
    final SimpleInstrument instrument = security.accept(CONVERTER).toDerivative(now);
    final CurrencyAmount pv = instrument.accept(CALCULATOR, data);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, _payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, _receiveCurveName)
        .with(ValuePropertyNames.CURRENCY, pv.getCurrency().getCode()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv.getAmount()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_FUTURE_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, _payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, _receiveCurveName)
        .with(ValuePropertyNames.CURRENCY, ((FXFutureSecurity) target.getSecurity()).getDenominator().getCode()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXFutureSecurity future = (FXFutureSecurity) target.getSecurity();
    final ValueRequirement payYieldCurve = YieldCurveFunction.getCurveRequirement(future.getNumerator(), _payCurveName, null, null);
    final ValueRequirement receiveYieldCurve = YieldCurveFunction.getCurveRequirement(future.getDenominator(), _receiveCurveName, null, null);
    final ValueRequirement spot = ConventionBasedFXRateFunction.getSpotRateRequirement(future.getNumerator(), future.getDenominator());
    return Sets.newHashSet(payYieldCurve, receiveYieldCurve, spot);
  }

}
