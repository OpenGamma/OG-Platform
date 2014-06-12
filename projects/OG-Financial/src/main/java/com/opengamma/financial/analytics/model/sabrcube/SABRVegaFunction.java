/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.SABRVegaCalculationUtils;
import com.opengamma.financial.analytics.model.VegaMatrixUtils;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.cube.fitted.FittedSmileDataPoints;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Base class for functions that calculate vega for swaptions, CMS, cap/floors and cap/floor CMS spreads using the SABR model.
 *
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public abstract class SABRVegaFunction extends SABRFunction {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfigSource().getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, currency, curves, desiredValue);
    final Object alphaSensitivityObject = inputs.getValue(PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(PRESENT_VALUE_SABR_NU_SENSITIVITY);
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final Object sabrSurfacesObject = inputs.getValue(SABR_SURFACES);
    if (sabrSurfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
    final Object fittedDataPointsObject = inputs.getValue(VOLATILITY_CUBE_FITTED_POINTS);
    if (fittedDataPointsObject == null) {
      throw new OpenGammaRuntimeException("Could not get fitted points for cube");
    }
    final FittedSmileDataPoints fittedDataPoints = (FittedSmileDataPoints) fittedDataPointsObject;
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = sabrFittedSurfaces.getInverseJacobians();
    final DoubleLabelledMatrix2D alphaSensitivity = (DoubleLabelledMatrix2D) alphaSensitivityObject;
    final DoubleLabelledMatrix2D nuSensitivity = (DoubleLabelledMatrix2D) nuSensitivityObject;
    final DoubleLabelledMatrix2D rhoSensitivity = (DoubleLabelledMatrix2D) rhoSensitivityObject;
    final double expiry = alphaSensitivity.getXKeys()[0];
    final double maturity = alphaSensitivity.getYKeys()[0];
    final double alpha = alphaSensitivity.getValues()[0][0];
    final double nu = nuSensitivity.getValues()[0][0];
    final double rho = rhoSensitivity.getValues()[0][0];
    final InterpolatedDoublesSurface alphaSurface = data.getSABRParameter().getAlphaSurface();
    final Map<Double, Interpolator1DDataBundle> alphaDataBundle = (Map<Double, Interpolator1DDataBundle>) alphaSurface.getInterpolatorData();
    final InterpolatedDoublesSurface nuSurface = data.getSABRParameter().getNuSurface();
    final Map<Double, Interpolator1DDataBundle> nuDataBundle = (Map<Double, Interpolator1DDataBundle>) nuSurface.getInterpolatorData();
    final InterpolatedDoublesSurface rhoSurface = data.getSABRParameter().getRhoSurface();
    final Map<Double, Interpolator1DDataBundle> rhoDataBundle = (Map<Double, Interpolator1DDataBundle>) rhoSurface.getInterpolatorData();
    final DoublesPair expiryMaturity = DoublesPair.of(expiry, maturity);
    final String xInterpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String xLeftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String xRightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String yInterpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    final String yLeftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    final String yRightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    final Interpolator1D xInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(xInterpolatorName, xLeftExtrapolatorName, xRightExtrapolatorName);
    final Interpolator1D yInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(yInterpolatorName, yLeftExtrapolatorName, yRightExtrapolatorName);
    final GridInterpolator2D nodeSensitivityCalculator = new GridInterpolator2D(xInterpolator, yInterpolator);
    final Map<Double, DoubleMatrix2D> result = SABRVegaCalculationUtils.getVegaCube(alpha, rho, nu, alphaDataBundle, rhoDataBundle, nuDataBundle, inverseJacobians, expiryMaturity,
        nodeSensitivityCalculator);
    final DoubleLabelledMatrix3D labelledMatrix = VegaMatrixUtils.getVegaSwaptionCubeQuoteMatrix(fittedDataPoints.getFittedPoints(), result);
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency.getCode(), desiredValue);
    final ValueSpecification spec = new ValueSpecification(getValueRequirement(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, labelledMatrix));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> xInterpolators = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (xInterpolators == null || xInterpolators.size() != 1) {
      return null;
    }
    final Set<String> xLeftExtrapolators = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (xLeftExtrapolators == null || xLeftExtrapolators.size() != 1) {
      return null;
    }
    final Set<String> xRightExtrapolators = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (xRightExtrapolators == null || xRightExtrapolators.size() != 1) {
      return null;
    }
    final Set<String> yInterpolators = constraints.getValues(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    if (yInterpolators == null || yInterpolators.size() != 1) {
      return null;
    }
    final Set<String> yLeftExtrapolators = constraints.getValues(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    if (yLeftExtrapolators == null || yLeftExtrapolators.size() != 1) {
      return null;
    }
    final Set<String> yRightExtrapolators = constraints.getValues(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    if (yRightExtrapolators == null || yRightExtrapolators.size() != 1) {
      return null;
    }
    final Security security = target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueProperties sensitivityProperties = getSensitivityProperties(target, currency.getCode(), desiredValue);
    final String cubeDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String cubeSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String surfaceDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
    final String surfaceSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.toSpecification(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.toSpecification(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.toSpecification(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ComputationTargetSpecification.NULL,
        getFittedPointsProperties(cubeDefinitionName, cubeSpecificationName, surfaceDefinitionName, surfaceSpecificationName, fittingMethod)));
    return requirements;
  }

  @Override
  protected String getValueRequirement() {
    return ValueRequirementNames.VEGA_QUOTE_CUBE;
  }

  /**
   * Gets the value properties for the SABR parameter sensitivities.
   * @param target The target
   * @param currency The currency of the security
   * @param desiredValue The desired value
   * @return The value properties
   */
  protected abstract ValueProperties getSensitivityProperties(final ComputationTarget target, final String currency, final ValueRequirement desiredValue);

  /**
   * Gets the value properties for the fitted points of the cube
   * @param cubeDefinitionName The cube definition name
   * @param cubeSpecificationName The cube specification name
   * @param surfaceDefinitionName The surface definition name
   * @param surfaceSpecificationName The surface specification name
   * @param fittingMethod The SABR fitting method
   * @return The value properties
   */
  protected ValueProperties getFittedPointsProperties(final String cubeDefinitionName, final String cubeSpecificationName,
      final String surfaceDefinitionName, final String surfaceSpecificationName, final String fittingMethod) {
    return ValueProperties.builder()
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, cubeDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, cubeSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION, surfaceDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationName)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod).get();
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    throw new UnsupportedOperationException("Should never get here");
  }
}
