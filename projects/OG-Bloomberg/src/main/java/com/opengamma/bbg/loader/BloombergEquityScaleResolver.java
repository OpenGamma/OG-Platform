/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BloombergEquityScaleResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEquityScaleResolver.class);
  private static final Set<String> BBG_FIELD = Collections.singleton(BloombergConstants.FIELD_CRNCY);
  private final ReferenceDataProvider _referenceDataProvider;
  private final boolean _useTickerSubscriptions;
  
  private static final ImmutableSet<ExternalScheme> s_tickerSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK);

  /**
   * Creates a BloombergSecurityTypeResolver
   * 
   * @param referenceDataProvider the reference data provider, not null
   * @param bbgScheme the scheme to use, not null
   */
  public BloombergEquityScaleResolver(ReferenceDataProvider referenceDataProvider, ExternalScheme bbgScheme) {
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    ArgumentChecker.notNull(bbgScheme, "bbgScheme");
    _referenceDataProvider = referenceDataProvider;
    _useTickerSubscriptions = s_tickerSchemes.contains(bbgScheme);
  }

  public Map<ExternalIdBundle, Integer> getBloombergEquityScale(final Collection<ExternalIdBundle> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    final Map<ExternalIdBundle, Integer> result = Maps.newHashMap();
    final BiMap<String, ExternalIdBundle> bundle2Bbgkey = getSubIds(identifiers);

    Map<String, FudgeMsg> fwdScaleResult = ReferenceDataProviderUtils.getFields(bundle2Bbgkey.keySet(), BBG_FIELD, _referenceDataProvider);

    for (ExternalIdBundle identifierBundle : identifiers) {
      String bbgKey = bundle2Bbgkey.inverse().get(identifierBundle);
      if (bbgKey != null) {
        FudgeMsg fudgeMsg = fwdScaleResult.get(bbgKey);
        if (fudgeMsg != null) {
          String bbgCurncy = fudgeMsg.getString(BloombergConstants.FIELD_CRNCY);
          Integer scale = null;
          try {            
            if (Character.isLowerCase(bbgCurncy.charAt(2))) {
              scale = 100;
            } else {
              scale = 1;
            }
            result.put(identifierBundle, scale);
          } catch (NumberFormatException | IndexOutOfBoundsException e) {
            s_logger.warn("Could not parse CURNCY with value {}", bbgCurncy);
          } 
        }
      }
    }
    return result;

  }
  
  private BiMap<String, ExternalIdBundle> getSubIds(Collection<ExternalIdBundle> identifiers) {
    if (_useTickerSubscriptions) {
      BiMap<String, ExternalIdBundle> result = HashBiMap.create();
      for (ExternalIdBundle bundle : identifiers) {
        for (ExternalId id : bundle) {
          if (s_tickerSchemes.contains(id.getScheme())) {
            result.put(id.getValue(), bundle);
            break;
          }
        }
      }
      return result;
    } else {
      return BloombergDataUtils.convertToBloombergBuidKeys(identifiers, _referenceDataProvider);
    }
  }
  
}
