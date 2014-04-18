/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Defaults functions for SABR vega calculations.
 * @deprecated The functions to which these default apply are deprecated
 */
@Deprecated
public class SABRNoExtrapolationVegaDefaults extends SABRNoExtrapolationDefaults {
  private final String _xInterpolator;
  private final String _xLeftExtrapolator;
  private final String _xRightExtrapolator;
  private final String _yInterpolator;
  private final String _yLeftExtrapolator;
  private final String _yRightExtrapolator;

  public SABRNoExtrapolationVegaDefaults(final String fittingMethod, final String xInterpolator, final String xLeftExtrapolator,
      final String xRightExtrapolator, final String yInterpolator, final String yLeftExtrapolator, final String yRightExtrapolator,
      final String... currencyAndConfigNames) {
    super(fittingMethod, currencyAndConfigNames);
    ArgumentChecker.notNull(xInterpolator, "x interpolator");
    ArgumentChecker.notNull(xLeftExtrapolator, "x left extrapolator");
    ArgumentChecker.notNull(xRightExtrapolator, "x right extrapolator");
    ArgumentChecker.notNull(yInterpolator, "y interpolator");
    ArgumentChecker.notNull(yLeftExtrapolator, "y left extrapolator");
    ArgumentChecker.notNull(yRightExtrapolator, "y right extrapolator");
    _xInterpolator = xInterpolator;
    _xLeftExtrapolator = xLeftExtrapolator;
    _xRightExtrapolator = xRightExtrapolator;
    _yInterpolator = yInterpolator;
    _yLeftExtrapolator = yLeftExtrapolator;
    _yRightExtrapolator = yRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.VEGA_QUOTE_CUBE, InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
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
    final Set<String> properties = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (properties != null) {
      return properties;
    }
    return null;
  }
}
