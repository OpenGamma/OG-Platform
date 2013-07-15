/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.DATA_PROVIDER_UNKNOWN;
import static com.opengamma.bbg.BloombergConstants.DEFAULT_DATA_PROVIDER;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility to assist with resolving Bloomberg codes.
 */
public final class BloombergDomainIdentifierResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergDomainIdentifierResolver.class);
  /**
   * Prefixes used by Bloomberg.
   */
  private static final Map<ExternalScheme, String> SCHEME_MAP = new LinkedHashMap<ExternalScheme, String>();
  static {
    SCHEME_MAP.put(ExternalSchemes.BLOOMBERG_BUID, "/buid/");
    SCHEME_MAP.put(ExternalSchemes.BLOOMBERG_BUID_WEAK, "/buid/");
    SCHEME_MAP.put(ExternalSchemes.BLOOMBERG_TICKER, null);
    SCHEME_MAP.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, null);
    SCHEME_MAP.put(ExternalSchemes.BLOOMBERG_TCM, null);
    SCHEME_MAP.put(ExternalSchemes.ISIN, "/isin/");
    SCHEME_MAP.put(ExternalSchemes.CUSIP, "/cusip/");
  };

  /**
   * Restricted constructor.
   */
  private BloombergDomainIdentifierResolver() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an external ID to a bloomberg key.
   * 
   * @param externalId  the external ID to convert, not null
   * @return the Bloomberg key, not null
   */
  public static String toBloombergKey(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    
    ExternalScheme scheme = externalId.getScheme();
    if (SCHEME_MAP.containsKey(scheme)) {
      String prefix = SCHEME_MAP.get(scheme);
      String id  = externalId.getValue();
      return prefix != null ? prefix + id : id;
    }
    s_logger.warn("Unknown ExternalScheme {}", externalId);
    return externalId.getValue();
  }

  /**
   * Converts an external ID to a bloomberg key.
   * 
   * @param externalId  the external ID to convert, not null
   * @param dataProvider  the data provider, null or unknown calls {@link #toBloombergKey(ExternalId)}
   * @return the Bloomberg key, not null
   */
  public static String toBloombergKeyWithDataProvider(ExternalId externalId, String dataProvider) {
    ArgumentChecker.notNull(externalId, "externalId");
    if (dataProvider == null || dataProvider.contains(DATA_PROVIDER_UNKNOWN) || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER)) {
      return toBloombergKey(externalId);
    }
    
    ExternalScheme scheme = externalId.getScheme();
    if (SCHEME_MAP.containsKey(scheme)) {
      String prefix = SCHEME_MAP.get(scheme);
      StringBuilder buf = new StringBuilder();
      if (prefix != null) {
        buf.append(prefix);
      }
      if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER)) {
        String id  = externalId.getValue().toUpperCase(Locale.US);
        if (id.endsWith("EQUITY")) {
          buf.append(id);
        } else {
          String[] splits = id.split(" ");
          if (id.endsWith("CURNCY") || id.endsWith("INDEX")) {
            buf.append(splits[0]).append(" ").append(dataProvider);
          } else {
            buf.append(splits[0]).append("@").append(dataProvider);
          }
          for (int i = 1; i < splits.length; i++) {
            buf.append(" ").append(splits[i]);
          }
        }
      } else {
        buf.append(externalId.getValue()).append("@").append(dataProvider);
      }
      return buf.toString();
    }
    s_logger.warn("Unknown ExternalScheme {}", externalId);
    return externalId.getValue();
  }

  /**
   * Selects the preferred external ID from a bundle.
   * 
   * @param bundle  the bundle, not null
   * @return the preferred external ID, not null
   */
  public static ExternalId resolvePreferredIdentifier(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.isTrue(bundle.size() > 0, "Bundle must not be empty");
    
    for (ExternalScheme preferredScheme : SCHEME_MAP.keySet()) {
      ExternalId preferredIdentifier = bundle.getExternalId(preferredScheme);
      if (preferredIdentifier != null) {
        return preferredIdentifier;
      }
    }
    return null;
  }

}
