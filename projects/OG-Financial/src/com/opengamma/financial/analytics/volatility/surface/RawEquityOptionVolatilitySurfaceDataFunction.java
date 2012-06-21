/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.UniqueId;
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
    final UniqueId uid = target.getUniqueId();
    if (uid == null) {
      return false;
    }
    final String targetScheme = uid.getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
            targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()) ||
            targetScheme.equalsIgnoreCase(Currency.OBJECT_SCHEME)); // TODO Remove this one. It is here to test redundant nature of target as both these are in vol spec. See View: Case Variance Swap test target
  }

}
