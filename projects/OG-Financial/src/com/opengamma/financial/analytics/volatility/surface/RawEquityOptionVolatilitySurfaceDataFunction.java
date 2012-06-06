/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.money.Currency;

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
  public boolean isCorrectIdType(ComputationTarget target) {
    String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
            targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()) ||
            targetScheme.equalsIgnoreCase(Currency.OBJECT_SCHEME)
    );
  }

}
