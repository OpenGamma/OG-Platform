/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.YieldCurveFunctionUtils.getCurveRequirementForFXOption;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionFunctionUtils.getSurfaceRequirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertiesUtils;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.black.BlackDiscountingFXOptionFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyMatrixSpotSourcingFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for FX option calculations that use the Black model.
 * @deprecated Use classes that extends from {@link BlackDiscountingFXOptionFunction}
 */
@Deprecated
public abstract class FXOptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackFunction.class);
  /** Property name for the put curve */
  public static final String PUT_CURVE = "PutCurve";
  /** Property name for the call curve */
  public static final String CALL_CURVE = "CallCurve";
  /** Property name for the put curve calculation configuration */
  public static final String PUT_CURVE_CALC_CONFIG = "PutCurveCalculationConfig";
  /** Property name for the receive curve calculation configuration */
  public static final String CALL_CURVE_CALC_CONFIG = "CallCurveCalculationConfig";
  /** The value requirement produced by the function */
  private final String _valueRequirementName;

  /**
   * @param valueRequirementName The value requirement name, not null
   */
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
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName };
    } else {
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName };
    }
    final InstrumentDefinition<?> definition = security.accept(new ForexSecurityConverter(baseQuotePairs));
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final InstrumentDerivative fxOption = definition.toDerivative(now);

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
    return FinancialSecurityTypes.FX_OPTION_SECURITY
        .or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY)
        .or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof FXBarrierOptionSecurity) {
      if (((FXBarrierOptionSecurity) security).getSamplingFrequency().equals(SamplingFrequency.ONE_LOOK)) {
        return false;
      }
    }
    return true;
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
    final Set<String> surfaceNames = constraints.getValues(SURFACE);
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
    final ValueProperties otherProperties = ValueProperties.builder().get();
    final String putCurveName = Iterables.getOnlyElement(putCurveNames);
    final String callCurveName = Iterables.getOnlyElement(callCurveNames);
    final String putCurveCalculationConfig = Iterables.getOnlyElement(putCurveCalculationConfigs);
    final String callCurveCalculationConfig = Iterables.getOnlyElement(callCurveCalculationConfigs);
    final String surfaceName = Iterables.getOnlyElement(surfaceNames);
    final String interpolatorName = Iterables.getOnlyElement(interpolatorNames);
    final String leftExtrapolatorName = Iterables.getOnlyElement(leftExtrapolatorNames);
    final String rightExtrapolatorName = Iterables.getOnlyElement(rightExtrapolatorNames);
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirementForFXOption(ComputationTargetSpecification.of(putCurrency), putCurveName, putCurveCalculationConfig, true,
        otherProperties);
    final ValueRequirement callFundingCurve = getCurveRequirementForFXOption(ComputationTargetSpecification.of(callCurrency), callCurveName, callCurveCalculationConfig, false,
        otherProperties);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final ValueRequirement spotRequirements = CurrencyMatrixSpotSourcingFunction.getConversionRequirement(callCurrency, putCurrency);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL);
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(spotRequirements);
    requirements.add(putFundingCurve);
    requirements.add(callFundingCurve);
    requirements.add(fxVolatilitySurface);
    requirements.add(pairQuoteRequirement);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    String putCurveName = null;
    String putCurveCalculationConfig = null;
    String callCurveName = null;
    String callCurveCalculationConfig = null;
    final ValueProperties.Builder optionalProperties = ValueProperties.builder();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification specification = entry.getKey();
      final ValueRequirement requirement = entry.getValue();
      final ValueProperties constraints = requirement.getConstraints();
      if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        if (constraints.getProperties().contains(PUT_CURVE)) {
          putCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          putCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          final ValueProperties properties = ValuePropertiesUtils.removeAll(constraints.copy().get(), ValuePropertyNames.CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
          ValuePropertiesUtils.withAllOptional(optionalProperties, properties);
        } else if (constraints.getProperties().contains(CALL_CURVE)) {
          callCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          callCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
          final ValueProperties properties = ValuePropertiesUtils.removeAll(constraints.copy().get(), ValuePropertyNames.CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
          ValuePropertiesUtils.withAllOptional(optionalProperties, properties);
        }
      } else if (specification.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = specification.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
      }
    }
    if (putCurveName == null || callCurveName == null || currencyPairConfigName == null) {
      return null;
    }
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      s_logger.error("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
      return null;
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target,
        putCurveName, putCurveCalculationConfig, callCurveName, callCurveCalculationConfig, baseQuotePair, optionalProperties.get()).get());
    return Collections.singleton(resultSpec);
  }

  protected abstract ValueProperties.Builder getResultProperties(ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(ComputationTarget target, String putCurve, String putCurveCalculationConfig,
      String callCurve, String callCurveCalculationConfig, CurrencyPair baseQuotePair, ValueProperties optionalProperties);

  protected abstract ValueProperties.Builder getResultProperties(ComputationTarget target, ValueRequirement desiredValue, CurrencyPair baseQuotePair);

  //TODO clumsy. Push the execute() method down into the functions and have getDerivative() and getData() methods
  protected abstract Set<ComputedValue> getResult(InstrumentDerivative forex, ForexOptionDataBundle<?> data, ComputationTarget target,
      Set<ValueRequirement> desiredValues, FunctionInputs inputs, ValueSpecification spec, FunctionExecutionContext executionContext);

  protected final String getValueRequirementName() {
    return _valueRequirementName;
  }

}
