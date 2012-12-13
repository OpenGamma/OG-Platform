/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.Y_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_ERROR;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_FIXED_ALPHA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_FIXED_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_FIXED_NU;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_FIXED_RHO;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_START_ALPHA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_START_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_START_NU;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_START_RHO;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_ALPHA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_NU;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_RHO;

import java.util.BitSet;
import java.util.Set;

import org.slf4j.Logger;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 *
 */
public class SABRFittingPropertyUtils {

  public static ValueProperties addFittingProperties(final ValueProperties properties) {
    final ValueProperties result = properties.copy()
      .withAny(X_INTERPOLATOR_NAME)
      .withAny(Y_INTERPOLATOR_NAME)
      .withAny(LEFT_X_EXTRAPOLATOR_NAME)
      .withAny(LEFT_Y_EXTRAPOLATOR_NAME)
      .withAny(RIGHT_X_EXTRAPOLATOR_NAME)
      .withAny(RIGHT_Y_EXTRAPOLATOR_NAME)
      .withAny(PROPERTY_FIXED_ALPHA)
      .withAny(PROPERTY_FIXED_BETA)
      .withAny(PROPERTY_FIXED_NU)
      .withAny(PROPERTY_FIXED_RHO)
      .withAny(PROPERTY_USE_FIXED_ALPHA)
      .withAny(PROPERTY_USE_FIXED_BETA)
      .withAny(PROPERTY_USE_FIXED_NU)
      .withAny(PROPERTY_USE_FIXED_RHO)
      .withAny(PROPERTY_START_ALPHA)
      .withAny(PROPERTY_START_BETA)
      .withAny(PROPERTY_START_NU)
      .withAny(PROPERTY_START_RHO)
      .withAny(PROPERTY_ERROR)
      .get();
    return result;
  }

  public static boolean ensureFittingProperties(final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> xInterpolatorName = constraints.getValues(X_INTERPOLATOR_NAME);
    if (xInterpolatorName == null || xInterpolatorName.size() != 1) {
      return false;
    }
    final Set<String> yInterpolatorName = constraints.getValues(Y_INTERPOLATOR_NAME);
    if (yInterpolatorName == null || yInterpolatorName.size() != 1) {
      return false;
    }
    final Set<String> leftXExtrapolatorName = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
    if (leftXExtrapolatorName == null || leftXExtrapolatorName.size() != 1) {
      return false;
    }
    final Set<String> leftYExtrapolatorName = constraints.getValues(LEFT_Y_EXTRAPOLATOR_NAME);
    if (leftYExtrapolatorName == null || leftYExtrapolatorName.size() != 1) {
      return false;
    }
    final Set<String> rightXExtrapolatorName = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightXExtrapolatorName == null || rightXExtrapolatorName.size() != 1) {
      return false;
    }
    final Set<String> rightYExtrapolatorName = constraints.getValues(RIGHT_Y_EXTRAPOLATOR_NAME);
    if (rightYExtrapolatorName == null || rightYExtrapolatorName.size() != 1) {
      return false;
    }
    final Set<String> useFixedAlpha = constraints.getValues(PROPERTY_USE_FIXED_ALPHA);
    if (useFixedAlpha == null || useFixedAlpha.size() != 1) {
      return false;
    }
    final Set<String> useFixedBeta = constraints.getValues(PROPERTY_USE_FIXED_BETA);
    if (useFixedBeta == null || useFixedBeta.size() != 1) {
      return false;
    }
    final Set<String> useFixedNu = constraints.getValues(PROPERTY_USE_FIXED_NU);
    if (useFixedNu == null || useFixedNu.size() != 1) {
      return false;
    }
    final Set<String> useFixedRho = constraints.getValues(PROPERTY_USE_FIXED_RHO);
    if (useFixedRho == null || useFixedRho.size() != 1) {
      return false;
    }
    final Set<String> fixedAlpha = constraints.getValues(PROPERTY_FIXED_ALPHA);
    if (fixedAlpha != null && fixedAlpha.size() != 1) {
      return false;
    }
    final Set<String> fixedBeta = constraints.getValues(PROPERTY_FIXED_BETA);
    if (fixedBeta != null && fixedBeta.size() != 1) {
      return false;
    }
    final Set<String> fixedNu = constraints.getValues(PROPERTY_FIXED_NU);
    if (fixedNu != null && fixedNu.size() != 1) {
      return false;
    }
    final Set<String> fixedRho = constraints.getValues(PROPERTY_FIXED_RHO);
    if (fixedRho != null && fixedRho.size() != 1) {
      return false;
    }
    final Set<String> startAlpha = constraints.getValues(PROPERTY_START_ALPHA);
    if (startAlpha != null && startAlpha.size() != 1) {
      return false;
    }
    final Set<String> startBeta = constraints.getValues(PROPERTY_START_BETA);
    if (startBeta != null && startBeta.size() != 1) {
      return false;
    }
    final Set<String> startNu = constraints.getValues(PROPERTY_START_NU);
    if (startNu != null && startNu.size() != 1) {
      return false;
    }
    final Set<String> startRho = constraints.getValues(PROPERTY_START_RHO);
    if (startRho != null && startRho.size() != 1) {
      return false;
    }
    final Set<String> errors = constraints.getValues(PROPERTY_ERROR);
    if (errors == null || errors.size() != 1) {
      return false;
    }
    return true;
  }

  public static boolean ensureConsistentDefaults(final ValueRequirement desiredValue, final Logger logger) {
    final ValueProperties constraints = desiredValue.getConstraints();
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_ALPHA))) {
      if (constraints.getValues(PROPERTY_START_ALPHA) != null) {
        logger.error("Using fixed alpha but have provided a starting value");
        return false;
      }
      if (constraints.getValues(PROPERTY_FIXED_ALPHA) == null) {
        logger.error("Using fixed alpha but have not provided a fixed value");
        return false;
      }
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_BETA))) {
      if (constraints.getValues(PROPERTY_START_BETA) != null) {
        logger.error("Using fixed beta but have provided a starting value");
        return false;
      }
      if (constraints.getValues(PROPERTY_FIXED_BETA) == null) {
        logger.error("Using fixed beta but have not provided a fixed value");
        return false;
      }
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_RHO))) {
      if (constraints.getValues(PROPERTY_START_RHO) != null) {
        logger.error("Using fixed rho but have provided a starting value");
        return false;
      }
      if (constraints.getValues(PROPERTY_FIXED_RHO) == null) {
        logger.error("Using fixed rho but have not provided a fixed value");
        return false;
      }
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_NU))) {
      if (constraints.getValues(PROPERTY_START_NU) != null) {
        logger.error("Using fixed nu but have provided a starting value");
        return false;
      }
      if (constraints.getValues(PROPERTY_FIXED_NU) == null) {
        logger.error("Using fixed nu but have not provided a fixed value");
        return false;
      }
    }
    return false;
  }

  public static DoubleMatrix1D getStartingValues(final ValueRequirement desiredValue) {
    final double alpha, beta, rho, nu;
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_ALPHA))) {
      alpha = Double.parseDouble(desiredValue.getConstraint(PROPERTY_FIXED_ALPHA));
    } else {
      alpha = Double.parseDouble(desiredValue.getConstraint(PROPERTY_START_ALPHA));
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_BETA))) {
      beta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_FIXED_BETA));
    } else {
      beta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_START_BETA));
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_RHO))) {
      rho = Double.parseDouble(desiredValue.getConstraint(PROPERTY_FIXED_RHO));
    } else {
      rho = Double.parseDouble(desiredValue.getConstraint(PROPERTY_START_RHO));
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_NU))) {
      nu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_FIXED_NU));
    } else {
      nu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_START_NU));
    }
    return new DoubleMatrix1D(new double[] {alpha, beta, rho, nu});
  }

  public static BitSet getFixedValues(final ValueRequirement desiredValue) {
    final BitSet result = new BitSet(4);
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_ALPHA))) {
      result.set(0);
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_BETA))) {
      result.set(1);
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_RHO))) {
      result.set(2);
    }
    if (Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_USE_FIXED_NU))) {
      result.set(3);
    }
    return result;
  }

  public static Interpolator2D getInterpolator(final ValueRequirement desiredValue) {
    final String xInterpolatorName = desiredValue.getConstraint(X_INTERPOLATOR_NAME);
    final String yInterpolatorName = desiredValue.getConstraint(Y_INTERPOLATOR_NAME);
    final String leftXExtrapolatorName = desiredValue.getConstraint(LEFT_X_EXTRAPOLATOR_NAME);
    final String rightXExtrapolatorName = desiredValue.getConstraint(RIGHT_X_EXTRAPOLATOR_NAME);
    final String leftYExtrapolatorName = desiredValue.getConstraint(LEFT_Y_EXTRAPOLATOR_NAME);
    final String rightYExtrapolatorName = desiredValue.getConstraint(RIGHT_Y_EXTRAPOLATOR_NAME);
    final Interpolator1D xInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(xInterpolatorName, leftXExtrapolatorName, rightXExtrapolatorName);
    final Interpolator1D yInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(yInterpolatorName, leftYExtrapolatorName, rightYExtrapolatorName);
    return new GridInterpolator2D(xInterpolator, yInterpolator);
  }

}
