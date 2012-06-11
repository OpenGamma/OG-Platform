/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardSwapCurveMarketDataFunction;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBSwaptionVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SABRNonLinearSwaptionDeltaVolatilityCubeFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNonLinearSwaptionDeltaVolatilityCubeFittingFunction.class);
  public static final String PROPERTY_USE_FIXED_ALPHA = "UseFixedAlpha";
  public static final String PROPERTY_USE_FIXED_BETA = "UseFixedBeta";
  public static final String PROPERTY_USE_FIXED_RHO = "UseFixedRho";
  public static final String PROPERTY_USE_FIXED_NU = "UseFixedNu";
  public static final String PROPERTY_ALPHA_START_VALUE = "AlphaStartValue";
  public static final String PROPERTY_BETA_START_VALUE = "BetaStartValue";
  public static final String PROPERTY_RHO_START_VALUE = "RhoStartValue";
  public static final String PROPERTY_NU_START_VALUE = "NuStartValue";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if (target.getUniqueId() == null) {
      s_logger.error("Target unique id was null");
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_ALPHA_START_VALUE)
        .withAny(PROPERTY_BETA_START_VALUE)
        .withAny(PROPERTY_NU_START_VALUE)
        .withAny(PROPERTY_RHO_START_VALUE)
        .withAny(PROPERTY_USE_FIXED_ALPHA)
        .withAny(PROPERTY_USE_FIXED_BETA)
        .withAny(PROPERTY_USE_FIXED_NU)
        .withAny(PROPERTY_USE_FIXED_RHO)
        .withAny(ValuePropertyNames.CUBE)
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.Y_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, SurfaceAndCubeQuoteType.CALL_DELTA)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD, VolatilityDataFittingDefaults.NON_LINEAR_LEAST_SQUARES).get();
    final ValueSpecification sabrSurfaceSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), properties);
    return Sets.newHashSet(sabrSurfaceSpecification);
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
    if (cubeNames == null || cubeNames.size() != 1) {
      s_logger.error("Did not provide a single cube name; asked for {}", cubeNames);
      return null;
    }
    final Set<String> cubeDefinitions = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    if (cubeDefinitions == null || cubeDefinitions.size() != 1) {
      return null;
    }
    final String cubeName = cubeNames.iterator().next();
    final String uniqueId = target.getUniqueId().getValue();
    final String definitionName = cubeDefinitions.iterator().next();
    final String fullDefinitionName = definitionName + "_" + uniqueId;
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBSwaptionVolatilityCubeDefinitionSource definitionSource = new ConfigDBSwaptionVolatilityCubeDefinitionSource(configSource);
    final SwaptionVolatilityCubeDefinition<Object, Object, Object> definition = (SwaptionVolatilityCubeDefinition<Object, Object, Object>) definitionSource.getDefinition(fullDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition name " + fullDefinitionName);
    }
    final Set<String> cubeSpecifications = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    if (cubeSpecifications == null || cubeSpecifications.size() != 1) {
      return null;
    }
    final Set<String> xInterpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (xInterpolatorNames == null || xInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> xLeftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (xLeftExtrapolatorNames == null || xLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> xRightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (xRightExtrapolatorNames == null || xRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yInterpolatorNames = constraints.getValues(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    if (yInterpolatorNames == null || yInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yLeftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    if (yLeftExtrapolatorNames == null || yLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yRightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    if (yRightExtrapolatorNames == null || yRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveInterpolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> alphaStartValues = constraints.getValues(PROPERTY_ALPHA_START_VALUE);
    if (alphaStartValues == null || alphaStartValues.size() != 1) {
      return null;
    }
    final Set<String> betaStartValues = constraints.getValues(PROPERTY_BETA_START_VALUE);
    if (betaStartValues == null || betaStartValues.size() != 1) {
      return null;
    }
    final Set<String> rhoStartValues = constraints.getValues(PROPERTY_RHO_START_VALUE);
    if (rhoStartValues == null || rhoStartValues.size() != 1) {
      return null;
    }
    final Set<String> nuStartValues = constraints.getValues(PROPERTY_NU_START_VALUE);
    if (nuStartValues == null || nuStartValues.size() != 1) {
      return null;
    }
    final Set<String> fixedAlpha = constraints.getValues(PROPERTY_USE_FIXED_ALPHA);
    if (fixedAlpha == null || fixedAlpha.size() != 1) {
      return null;
    }
    final Set<String> fixedBeta = constraints.getValues(PROPERTY_USE_FIXED_BETA);
    if (fixedBeta == null || fixedBeta.size() != 1) {
      return null;
    }
    final Set<String> fixedRho = constraints.getValues(PROPERTY_USE_FIXED_RHO);
    if (fixedRho == null || fixedRho.size() != 1) {
      return null;
    }
    final Set<String> fixedNu = constraints.getValues(PROPERTY_USE_FIXED_NU);
    if (fixedNu == null || fixedNu.size() != 1) {
      return null;
    }
    final String specificationName = cubeSpecifications.iterator().next();
    final String curveName = forwardCurveNames.iterator().next();
    final ValueRequirement swaptionCubeRequirement = getCubeDataRequirement(target, cubeName, definitionName, specificationName);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(swaptionCubeRequirement);
    for (final Object tenor : definition.getXs()) {
      requirements.add(getForwardCurveRequirement(target, curveName, ((Tenor) tenor).getPeriod().toString()));
    }
    return requirements;
  }

  private ValueRequirement getCubeDataRequirement(final ComputationTarget target, final String cubeName, final String definitionName, final String specificationName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, definitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, specificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, SurfaceAndCubeQuoteType.CALL_DELTA)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE).get();
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties);
  }

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String curveName, final String forwardTenor) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR, forwardTenor).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }
}
