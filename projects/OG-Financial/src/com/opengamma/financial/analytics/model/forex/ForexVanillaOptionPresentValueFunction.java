/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.Clock;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.forex.ForexVanillaOptionSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexVanillaOptionPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final PresentValueForexCalculator CALCULATOR = PresentValueForexCalculator.getInstance();
  private final String _putCurveName;
  private final String _callCurveName;
  private final String _surfaceName;
  private final ForexVanillaOptionSecurityConverter _visitor;

  public ForexVanillaOptionPresentValueFunction(final String putCurveName, final String callCurveName, String surfaceName) {
    Validate.notNull(putCurveName, "put curve name");
    Validate.notNull(callCurveName, "call curve name");
    Validate.notNull(surfaceName, "surface name");
    _putCurveName = putCurveName;
    _callCurveName = callCurveName;
    _surfaceName = surfaceName;
    _visitor = new ForexVanillaOptionSecurityConverter();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FXOptionSecurity security = (FXOptionSecurity) target.getSecurity();
    final ForexConverter<?> definition = _visitor.visitFXOptionSecurity(security);

    //TODO the marked off area should be done in init()
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    final String putCurveName = _putCurveName + "_" + putCurrency.getCode();
    final String callCurveName = _callCurveName + "_" + callCurrency.getCode();
    final String[] curveNames = new String[] {putCurveName, callCurveName};
    final Object putCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(putCurrency, _putCurveName, _putCurveName, _putCurveName));
    if (putCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + putCurveName + " curve");
    }
    final YieldAndDiscountCurve putCurve = (YieldAndDiscountCurve) putCurveObject;
    final Object callCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(callCurrency, _callCurveName, _callCurveName, _callCurveName));
    if (callCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + callCurveName + " curve");
    }
    final YieldAndDiscountCurve callCurve = (YieldAndDiscountCurve) putCurveObject;
    final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {putCurve, callCurve};
    final ForexDerivative fxOption = definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(curveNames, curves);
    final String bbgCode = putCurrency.getCode() + callCurrency.getCode() + " Curncy"; //TODO urgently needs removing
    final Identifier spotIdentifier = Identifier.of(SecurityUtils.BLOOMBERG_TICKER, bbgCode);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    final double spot = (Double) spotObject;
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _surfaceName)
                                                       .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement fxVolatilitySurfaceRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    final Object[] objectTenors = fxVolatilitySurface.getXs();
    final Tenor[] tenors = convertTenors(objectTenors); //TODO why is the necessary?
    final Pair<Number, FXVolQuoteType>[] quotes = sortQuotes(fxVolatilitySurface.getYs());
    final int nPoints = tenors.length;
    final SmileDeltaParameter[] smile = new SmileDeltaParameter[nPoints];
    final int nSmiles = (quotes.length - 1) / 2;
    for (int i = 0; i < tenors.length; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      final double atm = fxVolatilitySurface.getVolatility(tenor, quotes[0]);
      final double[] deltas = new double[nSmiles];
      final double[] riskReversals = new double[nSmiles];
      final double[] butterflies = new double[nSmiles];
      //TODO this is gross
      for (int j = 1, k = 0; j < quotes.length; j += 2, k++) {
        deltas[k] = quotes[j].getFirst().doubleValue() / 100;
        riskReversals[k] = fxVolatilitySurface.getVolatility(tenors[i], quotes[j]);
        butterflies[k] = fxVolatilitySurface.getVolatility(tenors[i], quotes[j + 1]);
      }
      smile[i] = new SmileDeltaParameter(t, atm, deltas, riskReversals, butterflies);
    }
    final SmileDeltaTermStructureParameter smiles = new SmileDeltaTermStructureParameter(smile);
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(smiles, spot, yieldCurves);
    final MultipleCurrencyAmount presentValue = CALCULATOR.visit(fxOption, smileBundle);
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, _putCurveName)
                                                              .with(ValuePropertyNames.RECEIVE_CURVE, _callCurveName)
                                                              .with(ValuePropertyNames.SURFACE, _surfaceName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, presentValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final ValueRequirement putCurve = YieldCurveFunction.getCurveRequirement(fxOption.getPutCurrency(), _putCurveName, _putCurveName, _putCurveName);
    final ValueRequirement callCurve = YieldCurveFunction.getCurveRequirement(fxOption.getCallCurrency(), _callCurveName, _callCurveName, _callCurveName);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _surfaceName)
                                                             .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    final ValueRequirement fxVolatilitySurface = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final String bbgCode = fxOption.getPutCurrency().getCode() + fxOption.getCallCurrency().getCode() + " Curncy"; //TODO urgently needs removing
    final Identifier spotIdentifier = Identifier.of(SecurityUtils.BLOOMBERG_TICKER, bbgCode);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    return Sets.newHashSet(putCurve, callCurve, fxVolatilitySurface, spotRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, _putCurveName)
                                                              .with(ValuePropertyNames.RECEIVE_CURVE, _callCurveName)
                                                              .with(ValuePropertyNames.SURFACE, _surfaceName).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(),
        properties));
  }

  private Tenor[] convertTenors(final Object[] objectTenors) {
    final int n = objectTenors.length;
    final Tenor[] result = new Tenor[n];
    for (int i = 0; i < n; i++) {
      result[i] = (Tenor) objectTenors[i];
    }
    return result;
  }

  //TODO this should not be done in here
  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return ((double) period.getMonths()) / 12;
    }
    if (period.getDays() != 0) {
      return ((double) period.getDays()) / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  //TODO this is not the right way to do this
  @SuppressWarnings("unchecked")
  private Pair<Number, FXVolQuoteType>[] sortQuotes(final Object[] quotes) {
    final int n = quotes.length;
    @SuppressWarnings("rawtypes")
    final Pair[] sorted = new Pair[n];
    final SortedSet<Number> deltas = new TreeSet<Number>();
    for (final Object quote : quotes) {
      final Pair<Number, FXVolQuoteType> pair = (Pair<Number, FXVolQuoteType>) quote;
      deltas.add(pair.getFirst());
    }
    int i = 0;
    for (final Number delta : deltas) {
      if (delta.intValue() != 0) {
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL);
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY);
      } else {
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.ATM);
      }
    }
    return sorted;
  }
}
