/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.financial.analytics.model.YieldCurveFunctionUtils.getCurveRequirement;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public abstract class FXOptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  /** The name of the calculation method */
  public static final String BLACK_METHOD = "BlackMethod";
  /** Property name for the put curve */
  public static final String PUT_CURVE = "PutCurve";
  /** Property name for the call curve */
  public static final String CALL_CURVE = "CallCurve";
  /** Property name for the put curve calculation configuration */
  public static final String PUT_CURVE_CALC_CONFIG = "PutCurveCalculationConfig";
  /** Property name for the receive curve calculation configuration */
  public static final String CALL_CURVE_CALC_CONFIG = "CallCurveCalculationConfig";
  private final String _valueRequirementName;

  public FXOptionBlackFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // Create the derivative
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());

    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);

    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    final String[] allCurveNames;
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName};
    }
    final InstrumentDefinition<?> definition = security.accept(new ForexSecurityConverter(baseQuotePairs));
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final InstrumentDerivative fxOption = definition.toDerivative(now, allCurveNames);

    // Get market data
    final ForexOptionDataBundle<?> marketData = FXOptionFunctionUtils.buildMarketBundle(now, inputs, target, desiredValues);

    // Create the result specification
    final ValueProperties.Builder properties = getResultProperties(target, desiredValue, baseQuotePair);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());

    // Compute result
    return getResult(fxOption, marketData, target, desiredValues, inputs, spec, executionContext);
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
    final Security security = target.getSecurity();
    if (security instanceof FXBarrierOptionSecurity) { // ONE_LOOK's are handled by FXOneLookBarrierOptionBlackFunction
      if (((FXBarrierOptionSecurity) security).getSamplingFrequency().equals(SamplingFrequency.ONE_LOOK)) {
        return false;
      }
    }

    return security instanceof FXOptionSecurity
        || security instanceof FXBarrierOptionSecurity
        || security instanceof FXDigitalOptionSecurity
        || security instanceof NonDeliverableFXOptionSecurity
        || security instanceof NonDeliverableFXDigitalOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties(target);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationConfigs = constraints.getValues(PUT_CURVE_CALC_CONFIG);
    if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationConfigs = constraints.getValues(CALL_CURVE_CALC_CONFIG);
    if (callCurveCalculationConfigs == null || callCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final String putCurveName = putCurveNames.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    final String putCurveCalculationConfig = putCurveCalculationConfigs.iterator().next();
    final String callCurveCalculationConfig = callCurveCalculationConfigs.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final String interpolatorName = interpolatorNames.iterator().next();
    final String leftExtrapolatorName = leftExtrapolatorNames.iterator().next();
    final String rightExtrapolatorName = rightExtrapolatorNames.iterator().next();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(putCurrency, putCurveName, putCurveCalculationConfig);
    final ValueRequirement callFundingCurve = getCurveRequirement(callCurrency, callCurveName, callCurveCalculationConfig);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement spotRequirement = new ValueRequirement(ValueRequirementNames.SPOT_RATE, currencyPair);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId());
    return Sets.newHashSet(putFundingCurve, callFundingCurve, fxVolatilitySurface, spotRequirement, pairQuoteRequirement);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair);

  //TODO clumsy. Push the execute() method down into the functions and have getDerivative() and getData() methods
  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext);

  protected static ValueRequirement getSurfaceRequirement(final String surfaceName, final Currency putCurrency, final Currency callCurrency,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
  }

  protected final String getValueRequirementName() {
    return _valueRequirementName;
  }
}
