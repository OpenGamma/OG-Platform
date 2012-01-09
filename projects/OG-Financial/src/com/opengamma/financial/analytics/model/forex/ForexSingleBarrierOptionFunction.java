/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public abstract class ForexSingleBarrierOptionFunction extends ForexOptionFunction {

  @Override
  protected InstrumentDefinition<?> getDefinition(final FinancialSecurity target) {
    final FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target;
    return getVisitor().visitFXBarrierOptionSecurity(security);
  }

  @Override
  protected Currency getPutCurrency(final FinancialSecurity target) {
    final FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target;
    return security.getPutCurrency();
  }

  @Override
  protected Currency getCallCurrency(final FinancialSecurity target) {
    final FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target;
    return security.getCallCurrency();
  }

  @Override
  protected ExternalId getSpotIdentifier(final FinancialSecurity target) {
    final FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target;
    return FXUtils.getSpotIdentifier(security, true);
  }

  @Override
  protected ExternalId getInverseSpotIdentifier(final FinancialSecurity target) {
    final FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target;
    return FXUtils.getInverseSpotIdentifier(security, true);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXBarrierOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXBarrierOptionSecurity fxOption = (FXBarrierOptionSecurity) target.getSecurity();
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
    final ValueRequirement putFundingCurve = YieldCurveFunction.getCurveRequirement(fxOption.getPutCurrency(), putFundingCurveName, putForwardCurveName, putFundingCurveName);
    final ValueRequirement putForwardCurve = YieldCurveFunction.getCurveRequirement(fxOption.getPutCurrency(), putForwardCurveName, putForwardCurveName, putFundingCurveName);
    final ValueRequirement callFundingCurve = YieldCurveFunction.getCurveRequirement(fxOption.getCallCurrency(), callFundingCurveName, callForwardCurveName, callFundingCurveName);
    final ValueRequirement callForwardCurve = YieldCurveFunction.getCurveRequirement(fxOption.getCallCurrency(), callForwardCurveName, callForwardCurveName, callFundingCurveName);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "FX_VANILLA_OPTION").get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(fxOption.getPutCurrency(), fxOption.getCallCurrency());
    final ValueRequirement fxVolatilitySurface = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, currenciesTarget, surfaceProperties);
    final ExternalId spotIdentifier = FXUtils.getSpotIdentifier(fxOption, true);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    final ExternalId inverseSpotIdentifier = FXUtils.getSpotIdentifier(fxOption);
    final ValueRequirement inverseSpotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, inverseSpotIdentifier);
    return Sets.newHashSet(putFundingCurve, putForwardCurve, callFundingCurve, callForwardCurve, fxVolatilitySurface, spotRequirement, inverseSpotRequirement);
  }
}
