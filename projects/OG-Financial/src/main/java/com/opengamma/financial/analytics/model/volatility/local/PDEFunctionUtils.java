/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_CENTRE_MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_MAX_MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_MAX_PROXY_DELTA;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_THETA;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;

/**
 *
 */
public class PDEFunctionUtils {

  public static Set<ValueRequirement> ensureForwardPDEFunctionProperties(final ValueProperties constraints) {
    final Set<String> thetas = constraints.getValues(PROPERTY_THETA);
    if (thetas == null || thetas.size() != 1) {
      return null;
    }
    final Set<String> nTimeSteps = constraints.getValues(PROPERTY_NUMBER_TIME_STEPS);
    if (nTimeSteps == null || nTimeSteps.size() != 1) {
      return null;
    }
    final Set<String> nSpaceSteps = constraints.getValues(PROPERTY_NUMBER_SPACE_STEPS);
    if (nSpaceSteps == null || nSpaceSteps.size() != 1) {
      return null;
    }
    final Set<String> timeStepBunchings = constraints.getValues(PROPERTY_TIME_STEP_BUNCHING);
    if (timeStepBunchings == null || timeStepBunchings.size() != 1) {
      return null;
    }
    final Set<String> spaceStepBunchings = constraints.getValues(PROPERTY_SPACE_STEPS_BUNCHING);
    if (spaceStepBunchings == null || spaceStepBunchings.size() != 1) {
      return null;
    }
    final Set<String> maxProxyDeltas = constraints.getValues(PROPERTY_MAX_PROXY_DELTA);
    if (maxProxyDeltas == null || maxProxyDeltas.size() != 1) {
      return null;
    }
    final Set<String> centreMoneynesses = constraints.getValues(PROPERTY_CENTRE_MONEYNESS);
    if (centreMoneynesses == null || centreMoneynesses.size() != 1) {
      return null;
    }
    final Set<String> directions = constraints.getValues(PROPERTY_PDE_DIRECTION);
    if (directions == null || directions.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(PROPERTY_SPACE_DIRECTION_INTERPOLATOR);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveNames = constraints.getValues(PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (discountingCurveCalculationConfigs == null || discountingCurveCalculationConfigs.size() != 1) {
      return null;
    }
    return Collections.emptySet();
  }

  public static ValueProperties.Builder addForwardPDEProperties(final ValueProperties properties) {
    return properties.copy()
        .withAny(PROPERTY_CENTRE_MONEYNESS)
        .withAny(PROPERTY_DISCOUNTING_CURVE_NAME)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_MAX_PROXY_DELTA)
        .withAny(PROPERTY_NUMBER_SPACE_STEPS)
        .withAny(PROPERTY_NUMBER_TIME_STEPS)
        .with(PROPERTY_PDE_DIRECTION, PDEPropertyNamesAndValues.FORWARDS)
        .withAny(PROPERTY_SPACE_DIRECTION_INTERPOLATOR)
        .withAny(PROPERTY_SPACE_STEPS_BUNCHING)
        .withAny(PROPERTY_THETA)
        .withAny(PROPERTY_TIME_STEP_BUNCHING);
  }

  public static ValueProperties.Builder addForwardPDEProperties(final ValueProperties properties, final ValueRequirement desiredValue) {
    return properties.copy()
        .with(PROPERTY_CENTRE_MONEYNESS, desiredValue.getConstraint(PROPERTY_CENTRE_MONEYNESS))
        .with(PROPERTY_DISCOUNTING_CURVE_NAME, desiredValue.getConstraint(PROPERTY_DISCOUNTING_CURVE_NAME))
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG))
        .with(PROPERTY_MAX_PROXY_DELTA, desiredValue.getConstraint(PROPERTY_MAX_PROXY_DELTA))
        .with(PROPERTY_NUMBER_SPACE_STEPS, desiredValue.getConstraint(PROPERTY_NUMBER_SPACE_STEPS))
        .with(PROPERTY_NUMBER_TIME_STEPS, desiredValue.getConstraint(PROPERTY_NUMBER_TIME_STEPS))
        .with(PROPERTY_PDE_DIRECTION, desiredValue.getConstraint(PROPERTY_PDE_DIRECTION))
        .with(PROPERTY_SPACE_DIRECTION_INTERPOLATOR, desiredValue.getConstraint(PROPERTY_SPACE_DIRECTION_INTERPOLATOR))
        .with(PROPERTY_SPACE_STEPS_BUNCHING, desiredValue.getConstraint(PROPERTY_SPACE_STEPS_BUNCHING))
        .with(PROPERTY_THETA, desiredValue.getConstraint(PROPERTY_THETA))
        .with(PROPERTY_TIME_STEP_BUNCHING, desiredValue.getConstraint(PROPERTY_TIME_STEP_BUNCHING));
  }

  public static Set<ValueRequirement> ensureBackwardPDEFunctionProperties(final ValueProperties constraints) {
    final Set<ValueRequirement> blackSurfaceRequirements = BlackVolatilitySurfacePropertyUtils.ensureAllBlackSurfaceProperties(constraints);
    if (blackSurfaceRequirements == null) {
      return null;
    }
    final Set<ValueRequirement> localSurfaceRequirements = LocalVolatilitySurfaceUtils.ensureDupireLocalVolatilitySurfaceProperties(constraints);
    if (localSurfaceRequirements == null) {
      return null;
    }
    final Set<String> thetas = constraints.getValues(PROPERTY_THETA);
    if (thetas == null || thetas.size() != 1) {
      return null;
    }
    final Set<String> nTimeSteps = constraints.getValues(PROPERTY_NUMBER_TIME_STEPS);
    if (nTimeSteps == null || nTimeSteps.size() != 1) {
      return null;
    }
    final Set<String> nSpaceSteps = constraints.getValues(PROPERTY_NUMBER_SPACE_STEPS);
    if (nSpaceSteps == null || nSpaceSteps.size() != 1) {
      return null;
    }
    final Set<String> timeStepBunchings = constraints.getValues(PROPERTY_TIME_STEP_BUNCHING);
    if (timeStepBunchings == null || timeStepBunchings.size() != 1) {
      return null;
    }
    final Set<String> spaceStepBunchings = constraints.getValues(PROPERTY_SPACE_STEPS_BUNCHING);
    if (spaceStepBunchings == null || spaceStepBunchings.size() != 1) {
      return null;
    }
    final Set<String> maxMoneynesses = constraints.getValues(PROPERTY_MAX_MONEYNESS);
    if (maxMoneynesses == null || maxMoneynesses.size() != 1) {
      return null;
    }
    final Set<String> directions = constraints.getValues(PROPERTY_PDE_DIRECTION);
    if (directions == null || directions.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(PROPERTY_SPACE_DIRECTION_INTERPOLATOR);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveNames = constraints.getValues(PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (discountingCurveCalculationConfigs == null || discountingCurveCalculationConfigs.size() != 1) {
      return null;
    }
    return Collections.emptySet();
  }

  public static ValueProperties.Builder addBackwardPDEProperties(final ValueProperties properties) {
    return properties.copy()
        .withAny(PROPERTY_DISCOUNTING_CURVE_NAME)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_MAX_MONEYNESS)
        .withAny(PROPERTY_NUMBER_SPACE_STEPS)
        .withAny(PROPERTY_NUMBER_TIME_STEPS)
        .with(PROPERTY_PDE_DIRECTION, PDEPropertyNamesAndValues.BACKWARDS)
        .withAny(PROPERTY_SPACE_DIRECTION_INTERPOLATOR)
        .withAny(PROPERTY_SPACE_STEPS_BUNCHING)
        .withAny(PROPERTY_THETA)
        .withAny(PROPERTY_TIME_STEP_BUNCHING);
  }

  public static ValueProperties.Builder addBackwardPDEProperties(final ValueProperties properties, final ValueRequirement desiredValue) {
    return properties.copy()
        .with(PROPERTY_DISCOUNTING_CURVE_NAME, desiredValue.getConstraint(PROPERTY_DISCOUNTING_CURVE_NAME))
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG))
        .with(PROPERTY_MAX_MONEYNESS, desiredValue.getConstraint(PROPERTY_MAX_MONEYNESS))
        .with(PROPERTY_NUMBER_SPACE_STEPS, desiredValue.getConstraint(PROPERTY_NUMBER_SPACE_STEPS))
        .with(PROPERTY_NUMBER_TIME_STEPS, desiredValue.getConstraint(PROPERTY_NUMBER_TIME_STEPS))
        .with(PROPERTY_PDE_DIRECTION, desiredValue.getConstraint(PROPERTY_PDE_DIRECTION))
        .with(PROPERTY_SPACE_DIRECTION_INTERPOLATOR, desiredValue.getConstraint(PROPERTY_SPACE_DIRECTION_INTERPOLATOR))
        .with(PROPERTY_SPACE_STEPS_BUNCHING, desiredValue.getConstraint(PROPERTY_SPACE_STEPS_BUNCHING))
        .with(PROPERTY_THETA, desiredValue.getConstraint(PROPERTY_THETA))
        .with(PROPERTY_TIME_STEP_BUNCHING, desiredValue.getConstraint(PROPERTY_TIME_STEP_BUNCHING));
  }
}
