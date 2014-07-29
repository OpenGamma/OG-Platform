/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.FLAT_SPLINE_EXTRAPOLATOR_FAILURE;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE;

import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * The Spline Smile extrapolates by fitting a ShiftedLogNormal Model. This is Black with an additional free parameter, F' = F*exp(mu)) <p>
 * <p>
 * Two choices are provided of how to handle ExtrapolatorFailureBehaviour if the fitter is unable to find a solution to fit the boundary vol and the vol gradient dVol/dK at that point...<p>
 * "Exception": an exception will be thrown. This selection puts the onus of shepherding the data on whoever provides marks. <p>
 * "Quiet":  the failing vol/strike will be tossed away, and the closest interior point is tried. This repeats until a solution is found. 
 * In this method, risk will not be attributed to failing vols.<p>
 * Or one can impose the trivial solution of flat vol extrapolation (dVoldK=0) in which vols outside range will return the value at the boundary.
 * "Flat": To select Flat extrapolation, set PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE =  
 */
public abstract class BlackVolatilitySurfaceSplineInterpolatorFunction extends BlackVolatilitySurfaceInterpolatorFunction {

  @Override
  protected Set<ValueRequirement> getSpecificRequirements(final ValueProperties constraints) {
    return BlackVolatilitySurfacePropertyUtils.ensureSplineVolatilityInterpolatorProperties(constraints);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    final ValueProperties.Builder properties = BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get(), desiredValue);
    return properties.get();
  }
  
  protected Interpolator1D getInterpolator1D(final ValueRequirement desiredValue) {
    final String interpolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_INTERPOLATOR);
    final String leftExtrapolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
    final String rightExtrapolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    return interpolator;
  }

/**
 * Fit ShiftedLognormal model to vol level and slope at the boundary.
 * If this fails, reduce slope until a fit is found. 
 */
  public static class Quiet extends BlackVolatilitySurfaceSplineInterpolatorFunction {
    @Override
    protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
      final Interpolator1D interpolator = getInterpolator1D(desiredValue);
      return new SmileInterpolatorSpline(interpolator, QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
    }
    @Override
    protected ValueProperties getResultProperties() {
      ValueProperties.Builder properties = BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get());
      properties = properties.withoutAny(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE) // Remove property set to 'any'
        .with(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE, QUIET_SPLINE_EXTRAPOLATOR_FAILURE); // Fix property to Quiet
      return properties.get();
    }
  }

  /**
   * Fit ShiftedLognormal model to vol level and slope at the boundary.
   * If this fails, throw exception
   */
  public static class Exception extends BlackVolatilitySurfaceSplineInterpolatorFunction {

    @Override
    protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
      final Interpolator1D interpolator = getInterpolator1D(desiredValue);
      return new SmileInterpolatorSpline(interpolator, EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE);
    }
    @Override
    protected ValueProperties getResultProperties() {
      ValueProperties.Builder properties = BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get());
      properties = properties.withoutAny(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE) // Remove property set to 'any'
        .with(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE, EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE); // Fix property to Exception
      return properties.get();
    }
  }
  
  /**
   * Fit trivial ShiftedLognormal model, where slope is zero, to vol level at the boundary.
   * This cannot fail to fit.
   */
  public static class Flat extends BlackVolatilitySurfaceSplineInterpolatorFunction {

    @Override
    protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
      final Interpolator1D interpolator = getInterpolator1D(desiredValue);
      return new SmileInterpolatorSpline(interpolator, FLAT_SPLINE_EXTRAPOLATOR_FAILURE);
    }
    @Override
    protected ValueProperties getResultProperties() {
      ValueProperties.Builder properties = BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get());
      properties = properties.withoutAny(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE) // Remove property set to 'any'
        .with(PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE, FLAT_SPLINE_EXTRAPOLATOR_FAILURE); // Fix property to Flat
      return properties.get();
    }
  }
}
