/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fxforwards.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.FX_PRESENT_VALUE;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXForwardPresentValueDiscountingFunction extends FXForwardDiscountingFunction {
  private static final PresentValueDiscountingCalculator CALCULATOR = PresentValueDiscountingCalculator.getInstance();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String payCurveConfigurationName = desiredValue.getConstraint("PAY_CURVE_BUNDLE");
    final ValueProperties payCurveBundleProperties = ValueProperties.builder()
        .with(CURVE_CONSTRUCTION_CONFIG, payCurveConfigurationName)
        .get();
    final String receiveCurveConfigurationName = desiredValue.getConstraint("RECEIVE_CURVE_BUNDLE");
    final ValueProperties receiveCurveBundleProperties = ValueProperties.builder()
        .with(CURVE_CONSTRUCTION_CONFIG, receiveCurveConfigurationName)
        .get();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Object baseQuotePairsObject = inputs.getValue(CURRENCY_PAIRS);
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final ForexSecurityConverter converter = new ForexSecurityConverter(baseQuotePairs);
    final InstrumentDefinition<?> definition = security.accept(converter);
    final Forex forex = (Forex) definition.toDerivative(now);
    final MulticurveProviderDiscount payCurveProvider = (MulticurveProviderDiscount) inputs.getValue(new ValueRequirement(CURVE_BUNDLE,
        ComputationTargetSpecification.NULL, payCurveBundleProperties));
    final MulticurveProviderDiscount receiveCurveProvider = (MulticurveProviderDiscount) inputs.getValue(new ValueRequirement(CURVE_BUNDLE,
        ComputationTargetSpecification.NULL, receiveCurveBundleProperties));
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(payCurveProvider);
    final Map<Currency, YieldAndDiscountCurve> discounting = receiveCurveProvider.getDiscountingCurves();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : discounting.entrySet()) {
      curves.setCurve(entry.getKey(), entry.getValue());
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIbor = receiveCurveProvider.getForwardIborCurves();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : forwardIbor.entrySet()) {
      curves.setCurve(entry.getKey(), entry.getValue());
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardON = receiveCurveProvider.getForwardONCurves();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardON.entrySet()) {
      curves.setCurve(entry.getKey(), entry.getValue());
    }
    final FXMatrix fx = curves.getFxRates();
    final FXMatrix receiveFX = receiveCurveProvider.getFxRates();
    final Set<Currency> currencies = receiveFX.getCurrencies().keySet();
    final Iterator<Currency> iter = currencies.iterator();
    final Currency ccy1 = iter.next();
    while (iter.hasNext()) {
      final Currency ccy2 = iter.next();
      fx.addCurrency(ccy1, ccy2, receiveFX.getFxRate(ccy1, ccy2));
    }
    curves.setForexMatrix(fx);
    final MultipleCurrencyAmount pv = forex.accept(CALCULATOR, curves);
    final ValueSpecification spec = new ValueSpecification(FX_PRESENT_VALUE, target.toSpecification(), desiredValue.getConstraints().copy().get());
    return Collections.singleton(new ComputedValue(spec, pv));
  }
}
