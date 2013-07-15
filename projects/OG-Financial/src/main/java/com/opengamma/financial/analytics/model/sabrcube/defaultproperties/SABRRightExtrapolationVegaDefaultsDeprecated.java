/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationFunctionDeprecated;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRRightExtrapolationVegaDefaults
 */
@Deprecated
public class SABRRightExtrapolationVegaDefaultsDeprecated extends DefaultPropertyFunction {
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String _cubeName;
  private final String _fittingMethod;
  private final String _curveCalculationMethod;
  private final String _cutoff;
  private final String _mu;
  private final String _xInterpolator;
  private final String _xLeftExtrapolator;
  private final String _xRightExtrapolator;
  private final String _yInterpolator;
  private final String _yLeftExtrapolator;
  private final String _yRightExtrapolator;
  private final String[] _applicableCurrencies;

  public SABRRightExtrapolationVegaDefaultsDeprecated(final String forwardCurveName, final String fundingCurveName, final String cubeName, final String fittingMethod,
      final String curveCalculationMethod, final String cutoff, final String mu, final String xInterpolator, final String xLeftExtrapolator, final String xRightExtrapolator,
      final String yInterpolator, final String yLeftExtrapolator, final String yRightExtrapolator, final String... applicableCurrencies) {
    super(FinancialSecurityTypes.SWAPTION_SECURITY.or(FinancialSecurityTypes.SWAP_SECURITY).or(FinancialSecurityTypes.CAP_FLOOR_SECURITY), true);
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(fundingCurveName, "funding curve name");
    ArgumentChecker.notNull(cubeName, "cube name");
    ArgumentChecker.notNull(fittingMethod, "fitting method");
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(cutoff, "cutoff");
    ArgumentChecker.notNull(mu, "mu");
    ArgumentChecker.notNull(xInterpolator, "x interpolator");
    ArgumentChecker.notNull(xLeftExtrapolator, "x left extrapolator");
    ArgumentChecker.notNull(xRightExtrapolator, "x right extrapolator");
    ArgumentChecker.notNull(yInterpolator, "y interpolator");
    ArgumentChecker.notNull(yLeftExtrapolator, "y left extrapolator");
    ArgumentChecker.notNull(yRightExtrapolator, "y right extrapolator");
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
    _cubeName = cubeName;
    _fittingMethod = fittingMethod;
    _curveCalculationMethod = curveCalculationMethod;
    _cutoff = cutoff;
    _mu = mu;
    _xInterpolator = xInterpolator;
    _xLeftExtrapolator = xLeftExtrapolator;
    _xRightExtrapolator = xRightExtrapolator;
    _yInterpolator = yInterpolator;
    _yLeftExtrapolator = yLeftExtrapolator;
    _yRightExtrapolator = yRightExtrapolator;
    _applicableCurrencies = applicableCurrencies;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof SwapSecurity) {
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType((SwapSecurity) security);
      if ((type != InterestRateInstrumentType.SWAP_FIXED_CMS) && (type != InterestRateInstrumentType.SWAP_CMS_CMS) && (type != InterestRateInstrumentType.SWAP_IBOR_CMS)) {
        return false;
      }
    }
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    for (final String ccy : _applicableCurrencies) {
      if (ccy.equals(currency)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, ValuePropertyNames.CUBE);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, SmileFittingProperties.PROPERTY_FITTING_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    }
    if (ValuePropertyNames.CUBE.equals(propertyName)) {
      return Collections.singleton(_cubeName);
    }
    if (SmileFittingProperties.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethod);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
    }
    if (SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE.equals(propertyName)) {
      return Collections.singleton(_cutoff);
    }
    if (SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER.equals(propertyName)) {
      return Collections.singleton(_mu);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xLeftExtrapolator);
    }
    if (InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yLeftExtrapolator);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xRightExtrapolator);
    }
    if (InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yRightExtrapolator);
    }
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xInterpolator);
    }
    if (InterpolatedDataProperties.Y_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yInterpolator);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SABR_FITTING_DEFAULTS;
  }

}
