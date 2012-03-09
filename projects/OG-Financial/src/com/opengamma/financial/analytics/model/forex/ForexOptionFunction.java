/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.forex.method.FXMatrix;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class ForexOptionFunction extends AbstractFunction.NonCompiledInvoker {
  /** The put funding curve property */
  public static final String PROPERTY_PUT_FUNDING_CURVE_NAME = "PutFundingCurve";
  /** The put forward curve property */
  public static final String PROPERTY_PUT_FORWARD_CURVE_NAME = "PutForwardCurve";
  /** The call funding curve property */
  public static final String PROPERTY_CALL_FUNDING_CURVE_NAME = "CallFundingCurve";
  /** The call forward curve property */
  public static final String PROPERTY_CALL_FORWARD_CURVE_NAME = "CallForwardCurve";
  /** The volatility surface name property */
  public static final String PROPERTY_FX_VOLATILITY_SURFACE_NAME = "FXVolatilitySurface";
  private ForexSecurityConverter _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    _visitor = new ForexSecurityConverter(OpenGammaCompilationContext.getSecuritySource(context));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ExternalId spotIdentifier = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    String putFundingCurveName = null;
    String putForwardCurveName = null;
    String callFundingCurveName = null;
    String callForwardCurveName = null;
    String surfaceName = null;
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    if (desiredValue.getConstraints().getValues(PROPERTY_PUT_FUNDING_CURVE_NAME) != null && !desiredValue.getConstraints().getValues(PROPERTY_PUT_FUNDING_CURVE_NAME).isEmpty()) {
      putFundingCurveName = desiredValue.getConstraint(PROPERTY_PUT_FUNDING_CURVE_NAME);
    }
    if (desiredValue.getConstraints().getValues(PROPERTY_PUT_FORWARD_CURVE_NAME) != null && !desiredValue.getConstraints().getValues(PROPERTY_PUT_FORWARD_CURVE_NAME).isEmpty()) {
      putForwardCurveName = desiredValue.getConstraint(PROPERTY_PUT_FORWARD_CURVE_NAME);
    }
    if (desiredValue.getConstraints().getValues(PROPERTY_CALL_FUNDING_CURVE_NAME) != null && !desiredValue.getConstraints().getValues(PROPERTY_CALL_FUNDING_CURVE_NAME).isEmpty()) {
      callFundingCurveName = desiredValue.getConstraint(PROPERTY_CALL_FUNDING_CURVE_NAME);
    }
    if (desiredValue.getConstraints().getValues(PROPERTY_CALL_FORWARD_CURVE_NAME) != null && !desiredValue.getConstraints().getValues(PROPERTY_CALL_FORWARD_CURVE_NAME).isEmpty()) {
      callForwardCurveName = desiredValue.getConstraint(PROPERTY_CALL_FORWARD_CURVE_NAME);
    }
    if (desiredValue.getConstraints().getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME) != null && !desiredValue.getConstraints().getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME).isEmpty()) {
      surfaceName = desiredValue.getConstraint(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
    }
    if (putFundingCurveName == null || putForwardCurveName == null || callFundingCurveName == null || callForwardCurveName == null || surfaceName == null) {
      for (final ComputedValue value : inputs.getAllValues()) {
        final ValueProperties properties = value.getSpecification().getProperties();
        if (properties.getValues(PROPERTY_PUT_FUNDING_CURVE_NAME) != null) {
          putFundingCurveName = properties.getValues(PROPERTY_PUT_FUNDING_CURVE_NAME).iterator().next();
        }
        if (properties.getValues(PROPERTY_PUT_FORWARD_CURVE_NAME) != null) {
          putForwardCurveName = properties.getValues(PROPERTY_PUT_FORWARD_CURVE_NAME).iterator().next();
        }
        if (properties.getValues(PROPERTY_CALL_FUNDING_CURVE_NAME) != null) {
          callFundingCurveName = properties.getValues(PROPERTY_CALL_FUNDING_CURVE_NAME).iterator().next();
        }
        if (properties.getValues(PROPERTY_CALL_FORWARD_CURVE_NAME) != null) {
          callForwardCurveName = properties.getValues(PROPERTY_CALL_FORWARD_CURVE_NAME).iterator().next();
        }
        if (properties.getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME) != null) {
          surfaceName = properties.getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME).iterator().next();
        }
      }
    }
    final String fullPutFundingCurveName = putFundingCurveName + "_" + putCurrency.getCode();
    final String fullCallFundingCurveName = callFundingCurveName + "_" + callCurrency.getCode();
    final String fullPutForwardCurveName = putForwardCurveName + "_" + putCurrency.getCode();
    final String fullCallForwardCurveName = callForwardCurveName + "_" + callCurrency.getCode();
    final String[] curveNames;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      curveNames = new String[] {fullPutFundingCurveName, fullCallFundingCurveName};
    } else {
      curveNames = new String[] {fullCallFundingCurveName, fullPutFundingCurveName};
    }
    final YieldAndDiscountCurve putFundingCurve = getCurve(inputs, putCurrency, putFundingCurveName);
    final YieldAndDiscountCurve putForwardCurve = getCurve(inputs, putCurrency, putForwardCurveName);
    final YieldAndDiscountCurve callFundingCurve = getCurve(inputs, callCurrency, callFundingCurveName);
    final YieldAndDiscountCurve callForwardCurve = getCurve(inputs, callCurrency, callForwardCurveName);
    final YieldAndDiscountCurve[] curves;
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, putForwardCurve, callFundingCurve, callForwardCurve};
      allCurveNames = new String[] {fullPutFundingCurveName, fullPutForwardCurveName, fullCallFundingCurveName, fullCallForwardCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, callForwardCurve, putFundingCurve, putForwardCurve};
      allCurveNames = new String[] {fullCallFundingCurveName, fullCallForwardCurveName, fullPutFundingCurveName, fullPutForwardCurveName};
      ccy1 = callCurrency;
      ccy2 = putCurrency;
    }
    final InstrumentDerivative fxOption = definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final Object spotObject = inputs.getValue(spotRequirement);
    double spot;
    if (spotObject == null) {
      final ExternalId inverseSpotIdentifier = security.accept(ForexVisitors.getInverseSpotIdentifierVisitor());
      final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
      final Object inverseSpotObject = inputs.getValue(inverseSpotRequirement);
      if (inverseSpotObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
      }
      spot = 1. / ((Double) inverseSpotObject);
    } else {
      spot = (Double) spotObject;
    }
    final ValueProperties surfaceProperties = ValueProperties
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX).get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement fxVolatilitySurfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final SmileDeltaTermStructureParameter smiles = (SmileDeltaTermStructureParameter) volatilitySurfaceObject;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(yieldCurves, fxMatrix, smiles, Pair.of(ccy1, ccy2));
    return getResult(fxOption, smileBundle, inputs, target, putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName);
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
    return target.getSecurity() instanceof FXOptionSecurity ||
        target.getSecurity() instanceof FXBarrierOptionSecurity ||
        target.getSecurity() instanceof FXDigitalOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties(target);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Set<String> putFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PUT_FUNDING_CURVE_NAME);
    if (putFundingCurveNames == null || putFundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_PUT_FORWARD_CURVE_NAME);
    if (putForwardCurveNames == null || putForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callFundingCurveNames = desiredValue.getConstraints().getValues(PROPERTY_CALL_FUNDING_CURVE_NAME);
    if (callFundingCurveNames == null || callFundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callForwardCurveNames = desiredValue.getConstraints().getValues(PROPERTY_CALL_FORWARD_CURVE_NAME);
    if (callForwardCurveNames == null || callForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String putFundingCurveName = putFundingCurveNames.iterator().next();
    final String putForwardCurveName = putForwardCurveNames.iterator().next();
    final String callFundingCurveName = callFundingCurveNames.iterator().next();
    final String callForwardCurveName = callForwardCurveNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(putFundingCurveName, PROPERTY_PUT_FUNDING_CURVE_NAME, putCurrency);
    final ValueRequirement putForwardCurve = getCurveRequirement(putForwardCurveName, PROPERTY_PUT_FORWARD_CURVE_NAME, putCurrency);
    final ValueRequirement callFundingCurve = getCurveRequirement(callFundingCurveName, PROPERTY_CALL_FUNDING_CURVE_NAME, callCurrency);
    final ValueRequirement callForwardCurve = getCurveRequirement(callForwardCurveName, PROPERTY_CALL_FORWARD_CURVE_NAME, callCurrency);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency);
    final ExternalId spotIdentifier = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final ExternalId inverseSpotIdentifier = security.accept(ForexVisitors.getInverseSpotIdentifierVisitor());
    final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
    return Sets.newHashSet(putFundingCurve, putForwardCurve, callFundingCurve, callForwardCurve, fxVolatilitySurface, spotRequirement, inverseSpotRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String putFundingCurveName = null;
    String putForwardCurveName = null;
    String callFundingCurveName = null;
    String callForwardCurveName = null;
    String surfaceName = null;
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      if (spec.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        if (spec.getTargetSpecification().getUniqueId().equals(putCurrency.getUniqueId())) {
          final Set<String> putCurveProperties = spec.getProperties().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
          if (putCurveProperties != null && !putCurveProperties.isEmpty()) {
            putFundingCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
            putForwardCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
          } else {
            putFundingCurveName = spec.getProperty(ValuePropertyNames.CURVE);
            putForwardCurveName = putFundingCurveName;
          }
        } else if (spec.getTargetSpecification().getUniqueId().equals(callCurrency.getUniqueId())) {
          final Set<String> callCurveProperties = spec.getProperties().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
          if (callCurveProperties != null && !callCurveProperties.isEmpty()) {
            callFundingCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
            callForwardCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
          } else {
            callFundingCurveName = spec.getProperty(ValuePropertyNames.CURVE);
            callForwardCurveName = callFundingCurveName;
          }
        }
      } else if (spec.getValueName().equals(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA)) {
        surfaceName = spec.getProperty(ValuePropertyNames.SURFACE);
      }
    }
    assert putFundingCurveName != null;
    assert putForwardCurveName != null;
    assert callFundingCurveName != null;
    assert callForwardCurveName != null;
    assert surfaceName != null;
    final ValueProperties.Builder properties = getResultProperties(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName, target);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  protected ValueProperties.Builder getResultProperties(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName, final ComputationTarget target) {
    return createValueProperties()
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  protected String getResultCurrency(final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    Currency ccy;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      ccy = putCurrency;
    } else {
      ccy = callCurrency;
    }
    return ccy.getCode();
  }

  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative forex, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName);

  protected abstract String getValueRequirementName();

  protected ValueRequirement getCurveRequirement(final String curveName, final String optional, final Currency currency) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .withOptional(optional);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected ValueRequirement getSurfaceRequirement(final String surfaceName, final Currency putCurrency, final Currency callCurrency) {
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX).get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
  }

  private YieldAndDiscountCurve getCurve(final FunctionInputs inputs, final Currency currency, final String curveName) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName);
    final Object curveObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get()));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
