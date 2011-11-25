/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;

/**
 * 
 */
public class Grid2DInterpolatedVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _definitionName;
  private final String _instrumentType;
  private final GridInterpolator2D _interpolator;
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private ValueRequirement _requirement;

  public Grid2DInterpolatedVolatilitySurfaceFunction(final String definitionName, final String instrumentType, final String tInterpolatorName, final String tLeftExtrapolatorName, 
      final String tRightExtrapolatorName, final String kInterpolatorName, final String kLeftExtrapolatorName, final String kRightExtrapolatorName) {
    Validate.notNull(definitionName, "definition name");
    Validate.notNull(instrumentType, "instrument type");
    Validate.notNull(tInterpolatorName, "t interpolator name");
    Validate.notNull(tLeftExtrapolatorName, "t left extrapolator name");
    Validate.notNull(tRightExtrapolatorName, "t right extrapolator name");
    Validate.notNull(kInterpolatorName, "k interpolator name");
    Validate.notNull(kLeftExtrapolatorName, "k left extrapolator name");
    Validate.notNull(kRightExtrapolatorName, "k right extrapolator name");
    _definitionName = definitionName;
    _instrumentType = instrumentType;
    Interpolator1D tInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(tInterpolatorName, tLeftExtrapolatorName, tRightExtrapolatorName);
    Interpolator1D kInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(kInterpolatorName, kLeftExtrapolatorName, kRightExtrapolatorName);
    _interpolator = new GridInterpolator2D(tInterpolator, kInterpolator);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _definition = volSurfaceDefinitionSource.getDefinition(_definitionName, _instrumentType);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Definition for " + _instrumentType + " called " + _definitionName);
    }
    ValueProperties surfaceProperties = ValueProperties.builder()
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get();
    _requirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, _definition.getTarget(), surfaceProperties);
    _result = new ValueSpecification(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(_definition.getTarget()),
        createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName).with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get());
    _results = Collections.singleton(_result);
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {

    return null;

    /* FIXME Case START HERE 
    ValueRequirement volatilitySurfaceRequirement = getVolatilitySurfaceRequirement();
    final Object volatilitySurfaceDataObject = inputs.getValue(volatilitySurfaceRequirement);
    if (volatilitySurfaceDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + volatilitySurfaceRequirement);
    }
    @SuppressWarnings("unchecked")
    VolatilitySurfaceData<Double, Double> volatilitySurfaceData = (VolatilitySurfaceData<Double, Double>) volatilitySurfaceDataObject;
    int n = volatilitySurfaceData.getXs().length;
    double[] t = new double[n];
    double[] k = new double[n];
    double[] sigma = new double[n];
    Double[] x = volatilitySurfaceData.getXs();
    Double[] y = volatilitySurfaceData.getYs();
    for (int i = 0; i < n; i++) {
      t[i] = x[i];
      k[i] = y[i];
      sigma[i] = volatilitySurfaceData.getVolatility(x[i], y[i]);
    }
    Surface<Double, Double, Double> surface = InterpolatedDoublesSurface.from(t, k, sigma, _interpolator);
    VolatilitySurface volatilitySurface = new VolatilitySurface(surface);
    return null;
    return Collections.singleton(new ComputedValue(getResultSpec(), volatilitySurface));
    */
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.singleton(_requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return _results;
  }
}
