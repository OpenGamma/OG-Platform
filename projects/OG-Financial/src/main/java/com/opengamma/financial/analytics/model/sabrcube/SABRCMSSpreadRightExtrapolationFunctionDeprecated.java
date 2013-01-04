/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRCMSSpreadRightExtrapolationFunction
 */
@Deprecated
public abstract class SABRCMSSpreadRightExtrapolationFunctionDeprecated extends SABRFunctionDeprecated {
  /** Property name for the cutoff strike after which extrapolation is used */
  public static final String PROPERTY_CUTOFF_STRIKE = "SABRExtrapolationCutoffStrike";
  /** Property name for the tail thickness parameter */
  public static final String PROPERTY_TAIL_THICKNESS_PARAMETER = "SABRTailThicknessParameter";

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CAP_FLOOR_CMS_SPREAD_SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffNames = constraints.getValues(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    if (cutoffNames == null || cutoffNames.size() != 1) {
      return null;
    }
    final Set<String> muNames = constraints.getValues(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (muNames == null || muNames.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    return requirements;
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency, final DayCount dayCount,
      final ValueRequirement desiredValue) {
    final YieldCurveBundle yieldCurves = getYieldCurves(inputs, currency, desiredValue);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingProperties.PROPERTY_FITTING_METHOD);
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DoubleFunction1D correlationFunction = getCorrelationFunction();
    final SABRInterestRateCorrelationParameters modelParameters = new SABRInterestRateCorrelationParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, correlationFunction);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy().with(ValuePropertyNames.CURRENCY, currency).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CUBE).withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD).withAny(SmileFittingProperties.PROPERTY_FITTING_METHOD)
        .with(SmileFittingProperties.PROPERTY_VOLATILITY_MODEL, SmileFittingProperties.SABR)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION).withAny(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE)
        .withAny(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingProperties.PROPERTY_FITTING_METHOD);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String cutoff = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    return properties.copy().with(ValuePropertyNames.CURRENCY, currency).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(ValuePropertyNames.CUBE, cubeName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(SmileFittingProperties.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingProperties.PROPERTY_VOLATILITY_MODEL, SmileFittingProperties.SABR)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION).with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE, cutoff)
        .with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER, mu).get();
  }

  private DoubleFunction1D getCorrelationFunction() {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return 0.8;
      }

    };
  }
}
