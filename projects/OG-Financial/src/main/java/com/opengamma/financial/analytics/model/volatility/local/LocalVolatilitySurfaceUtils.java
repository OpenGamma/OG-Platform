/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.DUPIRE_LOCAL_SURFACE_METHOD;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS_PARAMETERIZATION;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;

/**
 *
 */
public class LocalVolatilitySurfaceUtils {

  public static Set<ValueRequirement> ensureDupireLocalVolatilitySurfaceProperties(final ValueProperties constraints) {
    final Set<String> epsNames = constraints.getValues(PROPERTY_DERIVATIVE_EPS);
    if (epsNames == null || epsNames.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> blackSurfaceProperties = BlackVolatilitySurfacePropertyUtils.ensureAllBlackSurfaceProperties(constraints);
    if (blackSurfaceProperties == null) {
      return null;
    }
    return Collections.emptySet();
  }

  public static ValueProperties.Builder addAllDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String instrumentType, final String blackSmileInterpolator,
      final String parameterizationType) {
    final ValueProperties blackSurfaceProperties = BlackVolatilitySurfacePropertyUtils.addAllBlackSurfaceProperties(properties, instrumentType, blackSmileInterpolator).get();
    return addDupireLocalVolatilitySurfaceProperties(blackSurfaceProperties, parameterizationType);
  }

  public static ValueProperties.Builder addAllDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String instrumentType, final String blackSmileInterpolator,
      final String parameterizationType, final ValueRequirement desiredValue) {
    final String eps = desiredValue.getConstraint(PROPERTY_DERIVATIVE_EPS);
    final ValueProperties.Builder blackSurfaceProperties = BlackVolatilitySurfacePropertyUtils.addAllBlackSurfaceProperties(properties, instrumentType, desiredValue);
    return blackSurfaceProperties
      .with(PROPERTY_DERIVATIVE_EPS, eps)
      .with(PROPERTY_Y_AXIS_PARAMETERIZATION, parameterizationType)
      .with(PROPERTY_LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD, DUPIRE_LOCAL_SURFACE_METHOD);
  }

  public static ValueProperties.Builder addDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String parameterizationType) {
    return properties.copy()
      .withAny(PROPERTY_DERIVATIVE_EPS)
      .with(PROPERTY_Y_AXIS_PARAMETERIZATION, parameterizationType)
      .with(PROPERTY_LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD, DUPIRE_LOCAL_SURFACE_METHOD);
  }

  public static ValueProperties.Builder addDupireLocalVolatilitySurfaceProperties(final ValueProperties properties, final String parameterizationType, final ValueRequirement desiredValue) {
    final String eps = desiredValue.getConstraint(PROPERTY_DERIVATIVE_EPS);
    return properties.copy()
      .with(PROPERTY_DERIVATIVE_EPS, eps)
      .with(PROPERTY_Y_AXIS_PARAMETERIZATION, parameterizationType)
      .with(PROPERTY_LOCAL_VOLATILITY_SURFACE_CALCULATION_METHOD, DUPIRE_LOCAL_SURFACE_METHOD);
  }
}
