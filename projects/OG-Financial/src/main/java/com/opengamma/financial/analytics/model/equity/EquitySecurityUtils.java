/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for equity index option, equity barrier options and equity options.
 */
public final class EquitySecurityUtils {

  private EquitySecurityUtils() {
  }

  /**
   * Gets the underlying index or equity name from a security. At the moment, only securities with a BUID or Bloomberg ticker
   * are handled.
   * For a Bloomberg ticker, the suffix is stripped (SPX Index -> SPX). For a BUID, the last three letters are assumed to be
   * the name.
   * @param security The security, not null
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityName(final Security security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    String equity;
    final String value = underlyingId.getValue();
    final ExternalScheme scheme = underlyingId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_BUID) || scheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK)) {
      final int length = value.length();
      equity = value.substring(length - 3, length).toUpperCase(); //TODO fix this
    } else if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      equity = value.split(" ")[0];
    } else {
      throw new IllegalArgumentException("Cannot handle scheme of type " + scheme);
    }
    return equity;
  }

  /**
   * Gets the underlying index or equity name from a unique id. At the moment, only securities with a BUID or Bloomberg ticker
   * are handled.
   * For a Bloomberg ticker, the suffix is stripped (SPX Index -> SPX). For a BUID, the last three letters are assumed to be
   * the name.
   * @param uniqueId The unique id, not null
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityName(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "unique id");
    final String value = uniqueId.getValue();
    final String scheme = uniqueId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_BUID.getName()) || scheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName())) {
      final int length = value.length();
      return value.substring(length - 3, length).toUpperCase(); //TODO fix this
    }
    if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER.getName()) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
      return value.split(" ")[0];
    }
    return null;
  }

}
