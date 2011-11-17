/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.method.FXMatrix;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class ForexOptionFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _putFundingCurveName;
  private final String _putForwardCurveName;
  private final String _callFundingCurveName;
  private final String _callForwardCurveName;
  private final String _surfaceName;
  private ForexSecurityConverter _visitor;

  public ForexOptionFunction(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    Validate.notNull(putFundingCurveName, "put funding curve name");
    Validate.notNull(putForwardCurveName, "put forward curve name");
    Validate.notNull(callFundingCurveName, "call funding curve name");
    Validate.notNull(callForwardCurveName, "call forward curve name");
    Validate.notNull(surfaceName, "surface name");
    _putFundingCurveName = putFundingCurveName;
    _putForwardCurveName = putForwardCurveName;
    _callFundingCurveName = callFundingCurveName;
    _callForwardCurveName = callForwardCurveName;
    _surfaceName = surfaceName;
  }

  protected String getPutFundingCurveName() {
    return _putFundingCurveName;
  }

  protected String getPutForwardCurveName() {
    return _putForwardCurveName;
  }

  protected String getCallFundingCurveName() {
    return _callFundingCurveName;
  }

  protected String getCallForwardCurveName() {
    return _callForwardCurveName;
  }

  protected String getSurfaceName() {
    return _surfaceName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _visitor = new ForexSecurityConverter(OpenGammaCompilationContext.getSecuritySource(context));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ForexConverter<?> definition = getDefinition(security);
    final Currency putCurrency = getPutCurrency(security);
    final Currency callCurrency = getCallCurrency(security);
    final ExternalId spotIdentifier = getSpotIdentifier(security);
    final String putFundingCurveName = _putFundingCurveName + "_" + putCurrency.getCode();
    final String callFundingCurveName = _callFundingCurveName + "_" + callCurrency.getCode();
    final String putForwardCurveName = _putForwardCurveName + "_" + putCurrency.getCode();
    final String callForwardCurveName = _callForwardCurveName + "_" + callCurrency.getCode();
    final String[] curveNames;
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      curveNames = new String[] {putFundingCurveName, callFundingCurveName};
    } else {
      curveNames = new String[] {callFundingCurveName, putFundingCurveName};
    }
    final Object putFundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(putCurrency, _putFundingCurveName, _putForwardCurveName, _putFundingCurveName));
    if (putFundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + putFundingCurveName + " curve");
    }
    final YieldAndDiscountCurve putFundingCurve = (YieldAndDiscountCurve) putFundingCurveObject;
    final Object putForwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(putCurrency, _putForwardCurveName, _putForwardCurveName, _putFundingCurveName));
    if (putForwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + putForwardCurveName + " curve");
    }
    final YieldAndDiscountCurve putForwardCurve = (YieldAndDiscountCurve) putForwardCurveObject;
    final Object callFundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(callCurrency, _callFundingCurveName, _callForwardCurveName, _callFundingCurveName));
    if (callFundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + callFundingCurveName + " curve");
    }
    final YieldAndDiscountCurve callFundingCurve = (YieldAndDiscountCurve) callFundingCurveObject;
    final Object callForwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(callCurrency, _callForwardCurveName, _callForwardCurveName, _callFundingCurveName));
    if (callForwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + callForwardCurveName + " curve");
    }
    final YieldAndDiscountCurve callForwardCurve = (YieldAndDiscountCurve) callForwardCurveObject;
    final YieldAndDiscountCurve[] curves;
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, putForwardCurve, callFundingCurve, callForwardCurve};
      allCurveNames = new String[] {putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, callForwardCurve, putFundingCurve, putForwardCurve};
      allCurveNames = new String[] {callFundingCurveName, callForwardCurveName, putFundingCurveName, putForwardCurveName};
      ccy1 = callCurrency;
      ccy2 = putCurrency;
    }
    final ForexDerivative fxOption = definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final Object spotObject = inputs.getValue(spotRequirement);
    double spot;
    if (spotObject == null) {
      final ExternalId inverseSpotIdentifier = getInverseSpotIdentifier(security);
      final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
      final Object inverseSpotObject = inputs.getValue(inverseSpotRequirement);
      if (inverseSpotObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
      }
      spot = 1. / ((Double) inverseSpotObject);
    } else {
      spot = (Double) spotObject;
    }
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement fxVolatilitySurfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final SmileDeltaTermStructureParameter smiles = (SmileDeltaTermStructureParameter) volatilitySurfaceObject;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(yieldCurves, fxMatrix, smiles, Pair.of(ccy1, ccy2));
    return getResult(fxOption, smileBundle, inputs, target);
  }

  protected ForexSecurityConverter getVisitor() {
    return _visitor;
  }

  protected abstract Set<ComputedValue> getResult(ForexDerivative forex, SmileDeltaTermStructureDataBundle data, FunctionInputs inputs, ComputationTarget target);

  protected abstract ForexConverter<?> getDefinition(FinancialSecurity target);

  protected abstract Currency getPutCurrency(FinancialSecurity target);

  protected abstract Currency getCallCurrency(FinancialSecurity target);

  protected abstract ExternalId getSpotIdentifier(FinancialSecurity target);

  protected abstract ExternalId getInverseSpotIdentifier(FinancialSecurity target);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
