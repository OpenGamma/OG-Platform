/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;

/**
 *
 */
public class EquityIndexOptionDefaultPropertiesFunction extends DefaultPropertyFunction {

  private final String _volSurfaceName;
  private final String _fundingCurveName;
  private final String _smileInterpolator;

  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_VOMMA,
    ValueRequirementNames.VALUE_VANNA,
    ValueRequirementNames.VALUE_RHO
  };

  public EquityIndexOptionDefaultPropertiesFunction(final String volSurface, final String fundingCurve, final String smileInterpolator) {
    super(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY), true);
    Validate.notNull(volSurface, "No volSurface name was provided to use as default value.");
    Validate.notNull(fundingCurve, "No fundingCurve name was provided to use as default value.");
    _volSurfaceName = volSurface;
    _fundingCurveName = fundingCurve;
    _smileInterpolator = smileInterpolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueName, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    }
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    } else if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_volSurfaceName);
    } else if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_smileInterpolator);
    }
    return null;
  }

}
