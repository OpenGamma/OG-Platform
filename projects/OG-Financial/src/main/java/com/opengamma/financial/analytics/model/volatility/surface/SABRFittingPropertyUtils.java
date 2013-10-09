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
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_ALPHA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_ERROR;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_NU;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_RHO;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_ALPHA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_NU;
import static com.opengamma.financial.analytics.model.volatility.surface.SABRFittingProperties.PROPERTY_USE_FIXED_RHO;

import java.util.BitSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SABRFittingPropertyUtils {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SABRFittingPropertyUtils.class);

  public static ValueProperties.Builder addNLSSFittingProperties(final ValueProperties.Builder properties) {
    return properties
      .withAny(X_INTERPOLATOR_NAME)
      .withAny(Y_INTERPOLATOR_NAME)
      .withAny(LEFT_X_EXTRAPOLATOR_NAME)
      .withAny(LEFT_Y_EXTRAPOLATOR_NAME)
      .withAny(RIGHT_X_EXTRAPOLATOR_NAME)
      .withAny(RIGHT_Y_EXTRAPOLATOR_NAME)
      .withAny(PROPERTY_ALPHA)
      .withAny(PROPERTY_BETA)
      .withAny(PROPERTY_NU)
      .withAny(PROPERTY_RHO)
      .withAny(PROPERTY_USE_FIXED_ALPHA)
      .withAny(PROPERTY_USE_FIXED_BETA)
      .withAny(PROPERTY_USE_FIXED_NU)
      .withAny(PROPERTY_USE_FIXED_RHO)
      .withAny(PROPERTY_ERROR);
  }

  public static boolean ensureNLSSFittingProperties(final ValueRequirement desiredValue) {
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
    final Set<String> alpha = constraints.getValues(PROPERTY_ALPHA);
    if (alpha == null || alpha.size() != 1) {
      return false;
    }
    final Set<String> beta = constraints.getValues(PROPERTY_BETA);
    if (beta == null || beta.size() != 1) {
      return false;
    }
    final Set<String> nu = constraints.getValues(PROPERTY_NU);
    if (nu == null || nu.size() != 1) {
      return false;
    }
    final Set<String> rho = constraints.getValues(PROPERTY_RHO);
    if (rho == null || rho.size() != 1) {
      return false;
    }
    final Set<String> errors = constraints.getValues(PROPERTY_ERROR);
    if (errors == null || errors.size() != 1) {
      return false;
    }
    return true;
  }

  public static ValueRequirement getSurfaceRequirement(final ValueRequirement desiredValue, final String surfaceName, final Currency currency,
      final String instrumentType) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    if (fittingMethod == null) {
      s_logger.error("No value set for SABR fitting method");
      return null;
    }
    if (fittingMethod.equals(SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES)) {
      final ValueProperties.Builder allProperties = addNLSSFittingProperties(ValueProperties.builder())
          .with(ValuePropertyNames.CURRENCY, currency.getCode())
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
          .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
          .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES);
      for (final String constraint : constraints.getProperties()) {
        if (!constraints.getValues(constraint).isEmpty() && allProperties.get().getProperties().contains(constraint)) {
          allProperties
              .withoutAny(constraint)
              .with(constraint, constraints.getValues(constraint));
        }
      }
      return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, ComputationTargetSpecification.of(currency), allProperties.get());
    }
    s_logger.error("Could not handle fitting method {}", fittingMethod);
    return null;
  }

  public static DoubleMatrix1D getStartingValues(final ValueRequirement desiredValue) {
    final double alpha = Double.parseDouble(desiredValue.getConstraint(PROPERTY_ALPHA));
    final double beta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_BETA));
    final double rho = Double.parseDouble(desiredValue.getConstraint(PROPERTY_RHO));
    final double nu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_NU));
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
