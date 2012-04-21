/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class SABRRightExtrapolationFunction extends SABRFunction {
  /** Property name for the cutoff strike after which extrapolation is used */
  public static final String PROPERTY_CUTOFF_STRIKE = "SABRExtrapolationCutoffStrike";
  /** Property name for the tail thickness parameter */
  public static final String PROPERTY_TAIL_THICKNESS_PARAMETER = "SABRTailThicknessParameter";

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffNames = constraints.getValues(PROPERTY_CUTOFF_STRIKE);
    if (cutoffNames == null || cutoffNames.size() != 1) {
      return null;
    }
    final Set<String> muNames = constraints.getValues(PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (muNames == null || muNames.size() != 1) {
      return null;
    }
    return requirements;
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final ValueRequirement desiredValue) {
    final YieldCurveBundle yieldCurves = getYieldCurves(inputs, currency, desiredValue);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_TAIL_THICKNESS_PARAMETER));
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateExtrapolationParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, cutoff, mu);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CUBE)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION)
        .withAny(PROPERTY_CUTOFF_STRIKE)
        .withAny(PROPERTY_TAIL_THICKNESS_PARAMETER).get();
  }

  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final String forwardCurveName,
      final String fundingCurveName, final String cubeName, final String cutoff, final String mu) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION)
        .with(PROPERTY_CUTOFF_STRIKE, cutoff)
        .with(PROPERTY_TAIL_THICKNESS_PARAMETER, mu).get();
  }

}
