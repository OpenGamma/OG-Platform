/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public class SABRCMSSpreadNoExtrapolationVegaFunction extends SABRVegaFunction {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CAP_FLOOR_CMS_SPREAD_SECURITY;
  }

  @Override
  protected ValueProperties getSensitivityProperties(final ComputationTarget target, final String currency, final ValueRequirement desiredValue) {
    final String cubeDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String cubeSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String surfaceDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
    final String surfaceSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, cubeDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, cubeSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION, surfaceDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION).get();
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final YieldCurveBundle yieldCurves, final ValueRequirement desiredValue) {
    final Object surfacesObject = inputs.getValue(SABR_SURFACES);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR parameter surfaces");
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DoubleFunction1D correlationFunction = getCorrelationFunction();
    final SABRInterestRateCorrelationParameters modelParameters = new SABRInterestRateCorrelationParameters(alphaSurface, betaSurface, rhoSurface,
        nuSurface, correlationFunction);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.Y_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION)
        .withAny(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String cubeDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String cubeSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String surfaceDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
    final String surfaceSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String xInterpolator = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String xLeftExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String xRightExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String yInterpolator = desiredValue.getConstraint(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    final String yLeftExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    final String yRightExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, cubeDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, cubeSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION, surfaceDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, xInterpolator)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, xLeftExtrapolator)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, xRightExtrapolator)
        .with(InterpolatedDataProperties.Y_INTERPOLATOR_NAME, yInterpolator)
        .with(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME, yLeftExtrapolator)
        .with(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME, yRightExtrapolator)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .get();
  }

  private static DoubleFunction1D getCorrelationFunction() {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return 0.8;
      }

    };
  }
}
