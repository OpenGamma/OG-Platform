/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class CommodityFutureOptionUtils {

  /**
   * Commodity volatility surface uses ticker in name, extract that here
   * Will be obsoleted when commodity surface has a proper target
   *
   * @param security the security the surface is for
   * @param prefix beginning of the surface name
   * @return the surface name
   */
  public static String getSurfaceName(final FinancialSecurity security, final String prefix) {
    ExternalId ticker = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker == null) {
      ticker = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK);
    }
    if (ticker == null) {
      throw new OpenGammaRuntimeException("Couldn't get ticker for " + security);
    }
    return prefix + "_" + ticker.getValue().substring(0, 2);
  }

  public static String getSurfaceNameWithoutTicker(final FinancialSecurity security, final String fullName) {
    ExternalId ticker = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker == null) {
      ticker = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK);
    }
    if (ticker == null) {
      throw new OpenGammaRuntimeException("Couldn't get ticker for " + security);
    }
    final int index = fullName.lastIndexOf("_");
    return fullName.substring(0, index);
  }
}
