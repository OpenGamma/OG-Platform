/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube.defaults;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SABRNonLinearSwaptionVolatilityCubeFittingDefaults extends DefaultPropertyFunction {
  private final String _alphaStartValue;
  private final String _betaStartValue;
  private final String _rhoStartValue;
  private final String _nuStartValue;
  private final String _useFixedAlpha;
  private final String _useFixedBeta;
  private final String _useFixedRho;
  private final String _useFixedNu;
  private final String _eps;
  private final String _xInterpolator;
  private final String _xExtrapolator;
  private final String _yInterpolator;
  private final String _yExtrapolator;
  private final String _forwardCurveCalculationMethod;
  private final String _forwardCurveInterpolator;
  private final String _forwardCurveLeftExtrapolator;
  private final String _forwardCurveRightExtrapolator;

  public SABRNonLinearSwaptionVolatilityCubeFittingDefaults(final String alphaStartValue, final String betaStartValue, final String rhoStartValue, final String nuStartValue,
      final String useFixedAlpha, final String useFixedBeta, final String useFixedRho, final String useFixedNu, final String eps, final String xInterpolator, final String xExtrapolator,
      final String yInterpolator, final String yExtrapolator, final String forwardCurveCalculationMethod, final String forwardCurveInterpolator,
      final String forwardCurveLeftExtrapolator, final String forwardCurveRightExtrapolator) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(alphaStartValue, "alpha start value");
    ArgumentChecker.notNull(betaStartValue, "beta start value");
    ArgumentChecker.notNull(rhoStartValue, "rho start value");
    ArgumentChecker.notNull(nuStartValue, "nu start value");
    ArgumentChecker.notNull(useFixedAlpha, "use fixed alpha");
    ArgumentChecker.notNull(useFixedBeta, "use fixed beta");
    ArgumentChecker.notNull(useFixedRho, "use fixed rho");
    ArgumentChecker.notNull(useFixedNu, "use fixed nu");
    ArgumentChecker.notNull(eps, "eps");
    ArgumentChecker.notNull(xInterpolator, "x interpolator");
    ArgumentChecker.notNull(xExtrapolator, "x extrapolator");
    ArgumentChecker.notNull(yInterpolator, "y interpolator");
    ArgumentChecker.notNull(yExtrapolator, "y extrapolator");
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forward curve left extrapolator");
    ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forward curve right extrapolator");
    _alphaStartValue = alphaStartValue;
    _betaStartValue = betaStartValue;
    _rhoStartValue = rhoStartValue;
    _nuStartValue = nuStartValue;
    _useFixedAlpha = useFixedAlpha;
    _useFixedBeta = useFixedBeta;
    _useFixedRho = useFixedRho;
    _useFixedNu = useFixedNu;
    _eps = eps;
    _xInterpolator = xInterpolator;
    _xExtrapolator = xExtrapolator;
    _yInterpolator = yInterpolator;
    _yExtrapolator = yExtrapolator;
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _forwardCurveInterpolator = forwardCurveInterpolator;
    _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
    _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_ALPHA_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_ALPHA_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_BETA_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_BETA_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_RHO_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_RHO_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_NU_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_NU_START_VALUE);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_ALPHA);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_ALPHA);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_BETA);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_BETA);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_RHO);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_RHO);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_NU);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_NU);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_EPS);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_EPS);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, InterpolatedDataProperties.X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, InterpolatedDataProperties.X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.SABR_SURFACES, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_ALPHA_START_VALUE.equals(propertyName)) {
      return Collections.singleton(_alphaStartValue);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_BETA_START_VALUE.equals(propertyName)) {
      return Collections.singleton(_betaStartValue);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_RHO_START_VALUE.equals(propertyName)) {
      return Collections.singleton(_rhoStartValue);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_NU_START_VALUE.equals(propertyName)) {
      return Collections.singleton(_nuStartValue);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_ALPHA.equals(propertyName)) {
      return Collections.singleton(_useFixedAlpha);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_BETA.equals(propertyName)) {
      return Collections.singleton(_useFixedBeta);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_RHO.equals(propertyName)) {
      return Collections.singleton(_useFixedRho);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_USE_FIXED_NU.equals(propertyName)) {
      return Collections.singleton(_useFixedNu);
    }
    if (SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.PROPERTY_EPS.equals(propertyName)) {
      return Collections.singleton(_eps);
    }
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xInterpolator);
    }
    if (InterpolatedDataProperties.X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xExtrapolator);
    }
    if (InterpolatedDataProperties.Y_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yInterpolator);
    }
    if (InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yExtrapolator);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethod);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveInterpolator);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveLeftExtrapolator);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveRightExtrapolator);
    }
    return null;
  }

}
