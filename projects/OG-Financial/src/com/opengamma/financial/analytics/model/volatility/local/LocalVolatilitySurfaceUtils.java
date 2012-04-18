/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.DUPIRE_LOCAL_SURFACE_METHOD;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS_PARAMETERIZATION;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceUtils;

/**
 *
 */
public class LocalVolatilitySurfaceUtils {

  public static Set<ValueRequirement> ensureDupireLocalVolatilitySurfaceProperties(final ValueProperties constraints) {
    final Set<ValueRequirement> blackSurfaceRequirements = BlackVolatilitySurfaceUtils.ensureAllBlackSurfaceProperties(constraints);
    if (blackSurfaceRequirements == null) {
      return null;
    }
    final Set<String> epsNames = constraints.getValues(PROPERTY_DERIVATIVE_EPS);
    if (epsNames == null || epsNames.size() != 1) {
      return null;
    }
    return Collections.emptySet();
  }

  public static ValueProperties.Builder addDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String instrumentType, final String blackSmileInterpolator,
      final String parameterizationType) {
    final ValueProperties.Builder blackSurfaceProperties = BlackVolatilitySurfaceUtils.addAllBlackSurfaceProperties(properties, instrumentType, blackSmileInterpolator);
    return blackSurfaceProperties
      .withAny(PROPERTY_DERIVATIVE_EPS)
      .with(PROPERTY_Y_AXIS_PARAMETERIZATION, parameterizationType)
      .with(LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD, DUPIRE_LOCAL_SURFACE_METHOD);
  }

  public static ValueProperties.Builder addDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String instrumentType, final String blackSmileInterpolator,
      final String parameterizationType, final ValueRequirement desiredValue) {
    final String eps = desiredValue.getConstraint(PROPERTY_DERIVATIVE_EPS);
    final ValueProperties.Builder blackSurfaceProperties = BlackVolatilitySurfaceUtils.addAllBlackSurfaceProperties(properties, instrumentType, desiredValue);
    return blackSurfaceProperties
      .with(PROPERTY_DERIVATIVE_EPS, eps)
      .with(PROPERTY_Y_AXIS_PARAMETERIZATION, parameterizationType)
      .with(LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD, DUPIRE_LOCAL_SURFACE_METHOD);
  }
}
