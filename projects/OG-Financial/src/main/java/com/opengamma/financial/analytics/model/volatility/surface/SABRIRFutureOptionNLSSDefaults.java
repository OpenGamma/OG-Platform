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

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class SABRIRFutureOptionNLSSDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRIRFutureOptionNLSSDefaults.class);
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.SABR_SURFACES,
    ValueRequirementNames.VOLATILITY_SURFACE_FITTED_POINTS};
  private final String _xInterpolatorName;
  private final String _yInterpolatorName;
  private final String _leftXExtrapolatorName;
  private final String _rightXExtrapolatorName;
  private final String _leftYExtrapolatorName;
  private final String _rightYExtrapolatorName;
  private final String _useFixedAlpha;
  private final String _useFixedBeta;
  private final String _useFixedRho;
  private final String _useFixedNu;
  private final String _alpha;
  private final String _beta;
  private final String _rho;
  private final String _nu;
  private final String _error;

  public SABRIRFutureOptionNLSSDefaults(final String xInterpolatorName, final String yInterpolatorName,
      final String leftXExtrapolatorName, final String rightXExtrapolatorName, final String leftYExtrapolatorName, final String rightYExtrapolatorName,
      final String useFixedAlpha, final String useFixedBeta, final String useFixedRho, final String useFixedNu, final String alpha, final String beta,
      final String rho, final String nu, final String error) {
    super(ComputationTargetType.LEGACY_PRIMITIVE, true); // // [PLAT-2286]: change to correct type
    ArgumentChecker.notNull(xInterpolatorName, "x interpolator name");
    ArgumentChecker.notNull(yInterpolatorName, "y interpolator name");
    ArgumentChecker.notNull(leftXExtrapolatorName, "left x extrapolator name");
    ArgumentChecker.notNull(rightXExtrapolatorName, "right x extrapolator name");
    ArgumentChecker.notNull(leftYExtrapolatorName, "left y extrapolator name");
    ArgumentChecker.notNull(rightYExtrapolatorName, "right y extrapolator name");
    ArgumentChecker.notNull(useFixedAlpha, "use fixed alpha");
    ArgumentChecker.notNull(useFixedBeta, "use fixed beta");
    ArgumentChecker.notNull(useFixedRho, "use fixed rho");
    ArgumentChecker.notNull(useFixedNu, "use fixed nu");
    ArgumentChecker.notNull(alpha, "alpha");
    ArgumentChecker.notNull(beta, "beta");
    ArgumentChecker.notNull(rho, "rho");
    ArgumentChecker.notNull(nu, "nu");
    ArgumentChecker.notNull(error, "error");
    _xInterpolatorName = xInterpolatorName;
    _yInterpolatorName = yInterpolatorName;
    _leftXExtrapolatorName = leftXExtrapolatorName;
    _rightXExtrapolatorName = rightXExtrapolatorName;
    _leftYExtrapolatorName = leftYExtrapolatorName;
    _rightYExtrapolatorName = rightYExtrapolatorName;
    _useFixedAlpha = useFixedAlpha;
    _useFixedBeta = useFixedBeta;
    _useFixedRho = useFixedRho;
    _useFixedNu = useFixedNu;
    _alpha = alpha;
    _beta = beta;
    _rho = rho;
    _nu = nu;
    _error = error;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, Y_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, RIGHT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, LEFT_Y_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, RIGHT_Y_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueName, PROPERTY_USE_FIXED_ALPHA);
      defaults.addValuePropertyName(valueName, PROPERTY_USE_FIXED_BETA);
      defaults.addValuePropertyName(valueName, PROPERTY_USE_FIXED_RHO);
      defaults.addValuePropertyName(valueName, PROPERTY_USE_FIXED_NU);
      defaults.addValuePropertyName(valueName, PROPERTY_ALPHA);
      defaults.addValuePropertyName(valueName, PROPERTY_BETA);
      defaults.addValuePropertyName(valueName, PROPERTY_RHO);
      defaults.addValuePropertyName(valueName, PROPERTY_NU);
      defaults.addValuePropertyName(valueName, PROPERTY_ERROR);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xInterpolatorName);
    }
    if (Y_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yInterpolatorName);
    }
    if (LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftXExtrapolatorName);
    }
    if (RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightXExtrapolatorName);
    }
    if (LEFT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftYExtrapolatorName);
    }
    if (RIGHT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightYExtrapolatorName);
    }
    if (PROPERTY_USE_FIXED_ALPHA.equals(propertyName)) {
      return Collections.singleton(_useFixedAlpha);
    }
    if (PROPERTY_USE_FIXED_BETA.equals(propertyName)) {
      return Collections.singleton(_useFixedBeta);
    }
    if (PROPERTY_USE_FIXED_RHO.equals(propertyName)) {
      return Collections.singleton(_useFixedRho);
    }
    if (PROPERTY_USE_FIXED_NU.equals(propertyName)) {
      return Collections.singleton(_useFixedNu);
    }
    if (PROPERTY_ALPHA.equals(propertyName)) {
      return Collections.singleton(_alpha);
    }
    if (PROPERTY_BETA.equals(propertyName)) {
      return Collections.singleton(_beta);
    }
    if (PROPERTY_RHO.equals(propertyName)) {
      return Collections.singleton(_rho);
    }
    if (PROPERTY_NU.equals(propertyName)) {
      return Collections.singleton(_nu);
    }
    if (PROPERTY_ERROR.equals(propertyName)) {
      return Collections.singleton(_error);
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

  //TODO exclusion groups

}
