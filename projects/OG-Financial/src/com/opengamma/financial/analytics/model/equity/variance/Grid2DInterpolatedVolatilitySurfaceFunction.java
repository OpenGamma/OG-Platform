/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class Grid2DInterpolatedVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("rawtypes")
  private final GridInterpolator2D _interpolator;

  @SuppressWarnings({"unchecked", "rawtypes" })
  public Grid2DInterpolatedVolatilitySurfaceFunction(String tInterpolatorName, String tLeftExtrapolatorName, String tRightExtrapolatorName, String kInterpolatorName, String kLeftExtrapolatorName,
      String kRightExtrapolatorName) {
    Validate.notNull(tInterpolatorName, "t interpolator name");
    Validate.notNull(tLeftExtrapolatorName, "t left extrapolator name");
    Validate.notNull(tRightExtrapolatorName, "t right extrapolator name");
    Validate.notNull(kInterpolatorName, "k interpolator name");
    Validate.notNull(kLeftExtrapolatorName, "k left extrapolator name");
    Validate.notNull(kRightExtrapolatorName, "k right extrapolator name");
    Interpolator1D<Interpolator1DDataBundle> tInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(tInterpolatorName, tLeftExtrapolatorName, tRightExtrapolatorName);
    Interpolator1D<Interpolator1DDataBundle> kInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(kInterpolatorName, kLeftExtrapolatorName, kRightExtrapolatorName);
    _interpolator = new GridInterpolator2D(tInterpolator, kInterpolator);
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
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    //return Collections.singleton(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, ))
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return null;
  }

  //  private ValueRequirement getVolatilitySurfaceRequirement() {
  //    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, )
  //  }
}
