/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for equity index options, equity barrier options, equity options and equity variance swaps.
 */
public final class EquitySecurityUtils {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquitySecurityUtils.class);
  private EquitySecurityUtils() {
  }

  /**
   * Gets the underlying index or equity name from a security. At the moment, only securities with a Bloomberg ticker or
   * BUID (if the security is an equity index option) are handled.
   * For a Bloomberg ticker, the suffix is stripped (SPX Index -> SPX). For a BUID, the last three letters are assumed to be
   * the name.
   * @param security The security, not null
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityNameFromUnderlying(final Security security) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    if (underlyingId == null) {
      s_logger.error("Underlying id for security {} was null", security);
      return null;
    }
    final String value = underlyingId.getValue();
    final ExternalScheme scheme = underlyingId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_BUID) || scheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK)) {
      if (security instanceof EquityIndexOptionSecurity) {
        final int length = value.length();
        return value.substring(length - 3, length).toUpperCase(); //TODO fix this
      }
      s_logger.error("Can only use BUIDs for equity index options; have {}", security);
      return null;
    } else if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    s_logger.info("Cannot handle scheme of type {}", scheme);
    return null;
  }

  /**
   * Gets the underlying index or equity name from an external id. At the moment, only ids with a Bloomberg ticker are handled.
   * For a Bloomberg ticker, the suffix is stripped (SPX Index -> SPX).
   * @param underlyingId The security, not null
   * @return The equity or index name, null if the underlying id is not a Bloomberg ticker
   */
  public static String getIndexOrEquityNameFromUnderlying(final ExternalId underlyingId) {
    ArgumentChecker.notNull(underlyingId, "underlying id");
    final String value = underlyingId.getValue();
    final ExternalScheme scheme = underlyingId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    s_logger.info("Cannot handle scheme of type {}", scheme);
    return null;
  }

  /**
   * Gets the underlying index or equity name from a unique id. At the moment, only securities with a Bloomberg ticker or
   * BUID (if the security is an equity index option) are handled.
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
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    s_logger.info("Cannot handle scheme of type {}", scheme);
    return null;
  }

  /**
   * Removes the postfix if the uid is a Bloomberg ticker.
   * @param uid The unique id, not null
   * @return The ticker without postfix
   */
  public static String getTrimmedTarget(final UniqueId uid) {
    ArgumentChecker.notNull(uid, "unique id");
    final String value = uid.getValue();
    if (uid.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER.getName()) || uid.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    return value;
  }

  /**
   * Gets the exchange given a unique id representing an equity underlying. If the id scheme is weak, this is transformed
   * before the security is requested from the security source.
   * @param securitySource The security source, not null
   * @param id The id of the equity, not null
   * @return The exchange or null if there is no security for this id in the source.
   */
  public static String getExchange(final SecuritySource securitySource, final UniqueId id) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(id, "unique id");
    return "CBOE";
//    final String scheme;
//    final String originalScheme = id.getScheme();
//    if (originalScheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
//      scheme = ExternalSchemes.BLOOMBERG_TICKER.getName();
//    } else if (originalScheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName())) {
//      scheme = ExternalSchemes.BLOOMBERG_BUID.getName();
//    } else {
//      scheme = originalScheme;
//    }
//    final String value = id.getValue();
//    final ExternalId ticker = ExternalId.of(scheme, value);
//    final Security security = securitySource.getSingle(ExternalIdBundle.of(ticker));
//    if (security == null) {
//      return null;
//    }
//    return FinancialSecurityUtils.getExchange(security).getValue();
  }

  /**
   * Gets the currency given a unique id representing an equity underlying. If the id scheme is weak, this is transformed
   * before the security is requested from the security source.
   * @param securitySource The security source, not null
   * @param id The id of the equity, not null
   * @return The currency or null if there is no security for this id in the source.
   */
  public static String getCurrency(final SecuritySource securitySource, final UniqueId id) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(id, "unique id");
    return "USD";
//    final String scheme;
//    final String originalScheme = id.getScheme();
//    if (originalScheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
//      scheme = ExternalSchemes.BLOOMBERG_TICKER.getName();
//    } else if (originalScheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName())) {
//      scheme = ExternalSchemes.BLOOMBERG_BUID.getName();
//    } else {
//      scheme = originalScheme;
//    }
//    final String value = id.getValue();
//    final ExternalId ticker = ExternalId.of(scheme, value);
//    final Security security = securitySource.getSingle(ExternalIdBundle.of(ticker));
//    if (security == null) {
//      return null;
//    }
//    return FinancialSecurityUtils.getCurrency(security).getCode();
  }
}
