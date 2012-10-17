/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;

/**
 *
 */
public class RawEquityOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {

  /**
   * @param instrumentType
   */
  public RawEquityOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.EQUITY_OPTION);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // Bloomberg ticker or weak ticker
  }

  @Override
  protected boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()));
  }

}
