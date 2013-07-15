/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.conversion.ForexDomesticPipsToPresentValueConverter;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEFunction;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfaceUtils;
import com.opengamma.financial.analytics.model.volatility.local.PDEFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXOptionLocalVolatilityForwardPDEPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _blackSmileInterpolatorName;

  public FXOptionLocalVolatilityForwardPDEPresentValueFunction(final String blackSmileInterpolatorName) {
    ArgumentChecker.notNull(blackSmileInterpolatorName, "Black smile interpolator name");
    _blackSmileInterpolatorName = blackSmileInterpolatorName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    final double putAmount = fxOption.getPutAmount();
    final double callAmount = fxOption.getCallAmount();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueRequirement priceRequirement = getPriceRequirement(target, desiredValue);
    final Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new OpenGammaRuntimeException("Pips PV was null");
    }
    final ValueRequirement spotRequirement = getSpotRequirement(fxOption);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("FX spot rate was null");
    }
    final double spotFX = (Double) spotObject;
    final Double price = (Double) priceObject;
    final MultipleCurrencyAmount pvs = ForexDomesticPipsToPresentValueConverter.convertDomesticPipsToFXPresentValue(price, spotFX, putCurrency, callCurrency, putAmount, callAmount);
    final ValueProperties properties = getResultProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pvs));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = PDEFunctionUtils.ensureForwardPDEFunctionProperties(constraints);
    if (requirements == null) {
      return null;
    }
    final ValueRequirement priceRequirement = getPriceRequirement(target, desiredValue);
    final ValueRequirement spotRequirement = getSpotRequirement((FXOptionSecurity) target.getSecurity());
    return Sets.newHashSet(priceRequirement, spotRequirement);
  }

  private ValueRequirement getSpotRequirement(final FXOptionSecurity fxOption) {
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    return ConventionBasedFXRateFunction.getSpotRateRequirement(callCurrency, putCurrency);
  }

  private ValueRequirement getPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = getPriceProperties(desiredValue);
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
  }

  private ValueProperties getResultProperties() {
    ValueProperties result = createValueProperties().get();
    result = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(result, InstrumentTypeProperties.FOREX, _blackSmileInterpolatorName,
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS).get();
    result = PDEFunctionUtils.addForwardPDEProperties(result)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEFunction.CALCULATION_METHOD).get();
    return result;
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    ValueProperties result = createValueProperties().get();
    result = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(result, InstrumentTypeProperties.FOREX, _blackSmileInterpolatorName,
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS, desiredValue).get();
    result = PDEFunctionUtils.addForwardPDEProperties(result, desiredValue)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEFunction.CALCULATION_METHOD).get();
    return result;
  }

  private ValueProperties getPriceProperties(final ValueRequirement desiredValue) {
    ValueProperties result = ValueProperties.builder().get();
    result = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(result, InstrumentTypeProperties.FOREX, _blackSmileInterpolatorName,
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS, desiredValue).get();
    result = PDEFunctionUtils.addForwardPDEProperties(result, desiredValue)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEFunction.CALCULATION_METHOD).get();
    return result;
  }
}
