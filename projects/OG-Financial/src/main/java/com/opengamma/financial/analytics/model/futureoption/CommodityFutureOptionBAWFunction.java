/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.FinancialSecurity;

/**
 *
 */
public abstract class CommodityFutureOptionBAWFunction extends FutureOptionFunction {
  /** The calculation method name */
  public static final String BAW_METHOD = "BaroneAdesiWhaley";

  /**
   * @param valueRequirementName The value requirement name
   */
  public CommodityFutureOptionBAWFunction(final String... valueRequirementName) {
    super(valueRequirementName, BAW_METHOD);
  }

  @Override
  protected ValueRequirement getVolatilitySurfaceRequirement(final FinancialSecurity security, final String surfaceName, final String smileInterpolator, final String discountingCurveName,
      final String discountingCurveConfig) {
    return null;
  }

}
