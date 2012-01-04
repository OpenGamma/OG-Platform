/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.SABRVegaCalculationUtils;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.fitting.SurfaceFittedSmileDataPoints;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class InterestRateFutureOptionVegaFunction extends InterestRateFutureOptionFunction {
  private static final LinearInterpolator1D LINEAR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final GridInterpolator2D NODE_SENSITIVITY_CALCULATOR = new GridInterpolator2D(LINEAR, LINEAR);
  private ConfigDBVolatilitySurfaceDefinitionSource _volSurfaceDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    _volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
  }

  @SuppressWarnings({"unchecked" })
  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data, final ComputationTarget target,
      final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName, final String surfaceName) {
    final VolatilitySurfaceDefinition<?, ?> definition = _volSurfaceDefinitionSource.getDefinition(surfaceName, "IR_FUTURE_OPTION");
    if (definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find volatility surface definition for IR future option surface called " + surfaceName);
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(ccy.getCode(), forwardCurveName, fundingCurveName, surfaceName);
    final Object alphaSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final ValueRequirement surfacesRequirement = getSurfaceRequirement(target, surfaceName);
    final Object sabrSurfacesObject = inputs.getValue(surfacesRequirement);
    if (sabrSurfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final ValueRequirement fittedDataRequirement = getFittedDataRequirement(ccy, surfaceName);
    final Object fittedDataObject = inputs.getValue(fittedDataRequirement);
    if (fittedDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get fitted data points information");
    }
    final SurfaceFittedSmileDataPoints fittedData = (SurfaceFittedSmileDataPoints) fittedDataObject;
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
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
    final double[] expiryValues = alphaSurface.getXDataAsPrimitive();
    final DoubleMatrix2D result = SABRVegaCalculationUtils.getVegaSurface(alpha, rho, nu, alphaDataBundle, rhoDataBundle, nuDataBundle, inverseJacobians, expiryMaturity, NODE_SENSITIVITY_CALCULATOR,
        fittedData.getData(), (VolatilitySurfaceDefinition<Object, Object>) definition);
    final ValueSpecification resultSpec = getResultSpec(target, forwardCurveName, fundingCurveName, surfaceName);
    return Collections.singleton(new ComputedValue(resultSpec, VegaMatrixHelper.getVegaIRFutureOptionQuoteMatrixInStandardForm(definition, result, expiryValues)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(super.getRequirements(context, target, desiredValue));
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final String ccyCode = ccy.getCode();
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(ccyCode, forwardCurveName, fundingCurveName, surfaceName);
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(getFittedDataRequirement(ccy, surfaceName));
    return requirements;
  }

  private ValueSpecification getResultSpec(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get());
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName,
      final String surfaceName) {
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get());
  }

  private ValueProperties getModelSensitivityProperties(final String ccyCode, final String forwardCurveName, final String fundingCurveName, final String surfaceName) {
    return ValueProperties.builder().with(ValuePropertyNames.CURRENCY, ccyCode)
    .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
    .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
    .with(ValuePropertyNames.SURFACE, surfaceName).get();
  }

  private ValueRequirement getFittedDataRequirement(final Currency ccy, final String surfaceName) {
    final ValueProperties properties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.SURFACE, surfaceName)
      .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS, ccy, properties);
  }
}
