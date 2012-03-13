/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.DATA_PROVIDER_UNKNOWN;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author yomi
 */
public final class BloombergDomainIdentifierResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergDomainIdentifierResolver.class);
  private static final Map<ExternalScheme, String> DOMAIN_PREFERENCES = new LinkedHashMap<ExternalScheme, String>();
  
  static {
    DOMAIN_PREFERENCES.put(SecurityUtils.BLOOMBERG_BUID, "/buid/");
    DOMAIN_PREFERENCES.put(SecurityUtils.BLOOMBERG_BUID_WEAK, "/buid/");
    DOMAIN_PREFERENCES.put(SecurityUtils.BLOOMBERG_TICKER, null);
    DOMAIN_PREFERENCES.put(SecurityUtils.BLOOMBERG_TICKER_WEAK, null);
    DOMAIN_PREFERENCES.put(SecurityUtils.BLOOMBERG_TCM, null);
    DOMAIN_PREFERENCES.put(SecurityUtils.ISIN, "/isin/");
    DOMAIN_PREFERENCES.put(SecurityUtils.CUSIP, "/cusip/");
  };
 
  private BloombergDomainIdentifierResolver() {
  }
  
  public static String toBloombergKey(ExternalId identifier) {
    ArgumentChecker.notNull(identifier, "DomainSpecificIdentifier");
    String result = null;
    ExternalScheme domain = identifier.getScheme();
    if (DOMAIN_PREFERENCES.containsKey(domain)) {
      String prefix = DOMAIN_PREFERENCES.get(domain);
      result = prefix != null ? prefix + identifier.getValue() : identifier.getValue();
    } else {
      s_logger.warn("Unknown domainIdentifier {}", identifier);
      result = identifier.getValue();
    }
    return result;
  }
  
  public static String toBloombergKeyWithDataProvider(ExternalId identifier, String dataProvider) {
    if (dataProvider == null || dataProvider.contains(DATA_PROVIDER_UNKNOWN)) {
      return toBloombergKey(identifier);
    }
    
    ArgumentChecker.notNull(identifier, "DomainSpecificIdentifier");
    
    ExternalScheme domain = identifier.getScheme();
    String result = null;
    StringBuilder buf = new StringBuilder();
    
    if (DOMAIN_PREFERENCES.containsKey(domain)) {
      String prefix = DOMAIN_PREFERENCES.get(domain);
      if (prefix != null) {
        buf.append(prefix);
      }
      if (domain.equals(SecurityUtils.BLOOMBERG_TICKER)) {
        String id  = identifier.getValue().toUpperCase();
        String[] splits = id.split(" ");
        if (id.endsWith("CURNCY")) {
          buf.append(splits[0]).append(" ").append(dataProvider).append(" ");
        } else {
          buf.append(splits[0]).append("@").append(dataProvider).append(" ");
        }
        for (int i = 1; i < splits.length; i++) {
          buf.append(splits[i]).append(" ");
        }
        result =  buf.toString();
      } else {
        buf.append(identifier.getValue()).append("@").append(dataProvider);
        result = buf.toString();
      }
    } else {
      s_logger.warn("Unknown domainIdentifier {}", identifier);
      result = identifier.getValue();
    }
    return result;
  }

  public static ExternalId resolvePreferredIdentifier(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Validate.isTrue(bundle.size() > 0, "Bundle is empty");
    for (ExternalScheme preferredDomain : DOMAIN_PREFERENCES.keySet()) {
      ExternalId preferredIdentifier = bundle.getExternalId(preferredDomain);
      if (preferredIdentifier != null) {
        return preferredIdentifier;
      }
    }
    return null;
  }

}
