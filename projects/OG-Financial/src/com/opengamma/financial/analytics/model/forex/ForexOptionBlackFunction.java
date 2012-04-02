/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;
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
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class ForexOptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  /** The name of the calculation method */
  public static final String BLACK_METHOD = "BlackMethod";
  /** The put curve property */
  public static final String PROPERTY_PUT_CURVE = "PutCurve";
  /** The advisory forward put curve property */
  public static final String PROPERTY_PUT_FORWARD_CURVE = "PutForwardCurve";
  /** The property for the calculation method for the put currency curves */
  public static final String PROPERTY_PUT_CURVE_CALCULATION_METHOD = "PutCurveCalculationMethod";
  /** The call curve property */
  public static final String PROPERTY_CALL_CURVE = "CallCurve";
  /** The advisory forward call curve property */
  public static final String PROPERTY_CALL_FORWARD_CURVE = "CallForwardCurve";
  /** The property for the calculation method for the call currency curves */
  public static final String PROPERTY_CALL_CURVE_CALCULATION_METHOD = "CallCurveCalculationMethod";
  private static final ForexSecurityConverter VISITOR = new ForexSecurityConverter();
  private final String _valueRequirementName;

  public ForexOptionBlackFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(VISITOR);
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(PROPERTY_PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(PROPERTY_CALL_CURVE);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String putForwardCurveName = desiredValue.getConstraint(PROPERTY_PUT_FORWARD_CURVE);
    final String callForwardCurveName = desiredValue.getConstraint(PROPERTY_CALL_FORWARD_CURVE);
    final String putCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_PUT_CURVE_CALCULATION_METHOD);
    final String callCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_CALL_CURVE_CALCULATION_METHOD);
    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    final String[] curveNames;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      curveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curveNames = new String[] {fullCallCurveName, fullPutCurveName};
    }
    final YieldAndDiscountCurve putFundingCurve = getCurve(inputs, putCurrency, putCurveName);
    final YieldAndDiscountCurve callFundingCurve = getCurve(inputs, callCurrency, callCurveName);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    curveCurrency.put(fullPutCurveName, putCurrency);
    curveCurrency.put(fullCallCurveName, callCurrency);
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, callFundingCurve};
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, putFundingCurve};
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName};
      ccy1 = callCurrency;
      ccy2 = putCurrency;
    }
    final InstrumentDerivative fxOption = definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueRequirement spotRequirement = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot requirement " + spotRequirement);
    }
    final double spot = (Double) spotObject;
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX).get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement fxVolatilitySurfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final SmileDeltaTermStructureParameter smiles = (SmileDeltaTermStructureParameter) volatilitySurfaceObject;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final ValueProperties.Builder properties = getResultProperties(putCurveName, putForwardCurveName, putCurveCalculationMethod, callCurveName, callForwardCurveName,
        callCurveCalculationMethod, surfaceName, target);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(fxMatrix, curveCurrency, yieldCurves, smiles, Pair.of(ccy1, ccy2));
    return getResult(fxOption, smileBundle, spec);
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
    return target.getSecurity() instanceof FXOptionSecurity || target.getSecurity() instanceof FXBarrierOptionSecurity || target.getSecurity() instanceof FXDigitalOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(PROPERTY_PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(PROPERTY_CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> putForwardCurveNames = constraints.getValues(PROPERTY_PUT_FORWARD_CURVE);
    if (putForwardCurveNames == null || putForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callForwardCurveNames = constraints.getValues(PROPERTY_CALL_FORWARD_CURVE);
    if (callForwardCurveNames == null || callForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationMethods = constraints.getValues(PROPERTY_PUT_CURVE_CALCULATION_METHOD);
    if (putCurveCalculationMethods == null || putCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationMethods = constraints.getValues(PROPERTY_CALL_CURVE_CALCULATION_METHOD);
    if (callCurveCalculationMethods == null || callCurveCalculationMethods.size() != 1) {
      return null;
    }
    final String putCurveName = putCurveNames.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    final String putForwardCurveName = putForwardCurveNames.iterator().next();
    final String callForwardCurveName = callForwardCurveNames.iterator().next();
    final String putCurveCalculationMethod = putCurveCalculationMethods.iterator().next();
    final String callCurveCalculationMethod = callCurveCalculationMethods.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(putCurveName, putForwardCurveName, putCurveName, putCurveCalculationMethod, putCurrency);
    final ValueRequirement callFundingCurve = getCurveRequirement(callCurveName, callForwardCurveName, callCurveName, callCurveCalculationMethod, callCurrency);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency);
    final ValueRequirement spotRequirement = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    return Sets.newHashSet(putFundingCurve, callFundingCurve, fxVolatilitySurface, spotRequirement);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final ComputationTarget target);

  protected abstract Set<ComputedValue> getResult(InstrumentDerivative forex, SmileDeltaTermStructureDataBundle data, ValueSpecification spec);

  protected static ValueRequirement getCurveRequirement(final String curveName, final String forwardCurveName, final String fundingCurveName, final String calculationMethod, final Currency currency) {
    final ValueProperties.Builder properties;
    if (calculationMethod == InterpolatedCurveAndSurfaceProperties.CALCULATION_METHOD_NAME) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME)
          .withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
          .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected static ValueRequirement getSurfaceRequirement(final String surfaceName, final Currency putCurrency, final Currency callCurrency) {
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX).get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
  }

  private static YieldAndDiscountCurve getCurve(final FunctionInputs inputs, final Currency currency, final String curveName) {
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    final Object curveObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get()));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
