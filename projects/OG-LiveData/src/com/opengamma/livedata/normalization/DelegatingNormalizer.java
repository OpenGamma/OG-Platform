/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;

/**
 * A normalization service that uses the external ID scheme of each value to be normalized to choose between multiple
 * underlying normalization services.
 */
public class DelegatingNormalizer implements Normalizer {

  private static final Logger s_logger = LoggerFactory.getLogger(DelegatingNormalizer.class);
  
  private final Map<ExternalScheme, Normalizer> _normalizers;
  
  @SuppressWarnings("unchecked")
  public DelegatingNormalizer(final Map<?, ? extends Normalizer> normalizers) {
    _normalizers = new HashMap<ExternalScheme, Normalizer>();
    for (Map.Entry<?, ? extends Normalizer> normalizer : normalizers.entrySet()) {
      if (normalizer.getKey() instanceof Collection) {
        for (Object x : (Collection<Object>) normalizer.getKey()) {
          _normalizers.put(toExternalScheme(x), normalizer.getValue());
        }
      } else {
        _normalizers.put(toExternalScheme(normalizer.getKey()), normalizer.getValue());
      }
    }
  }
  
  @Override
  public FudgeMsg normalizeValues(LiveDataSpecification specification, FudgeMsg data) {
    return normalizeValues(ImmutableMap.of(specification, data)).get(specification);
  }

  @Override
  public Map<LiveDataSpecification, FudgeMsg> normalizeValues(Map<LiveDataSpecification, ? extends FudgeMsg> data) {
    Map<LiveDataSpecification, FudgeMsg> combinedResults = new HashMap<LiveDataSpecification, FudgeMsg>();
    Map<Normalizer, Map<LiveDataSpecification, FudgeMsg>> bucketedData = new HashMap<Normalizer, Map<LiveDataSpecification, FudgeMsg>>();
    for (Map.Entry<LiveDataSpecification, ? extends FudgeMsg> dataEntry : data.entrySet()) {
      Normalizer normalizer = getNormalizer(dataEntry.getKey());
      if (normalizer == null) {
        // Pass through unnormalized
        s_logger.warn("No normalizer found for {}; passing value through unnormalized", dataEntry.getKey());
        combinedResults.put(dataEntry.getKey(), dataEntry.getValue());
        continue;
      }
      Map<LiveDataSpecification, FudgeMsg> normalizerData = bucketedData.get(normalizer);
      if (normalizerData == null) {
        normalizerData = new HashMap<LiveDataSpecification, FudgeMsg>();
        bucketedData.put(normalizer, normalizerData);
      }
      normalizerData.put(dataEntry.getKey(), dataEntry.getValue());
    }
    for (Map.Entry<Normalizer, Map<LiveDataSpecification, FudgeMsg>> bucket : bucketedData.entrySet()) {
      s_logger.debug("Using {} to normalize {}", bucket.getKey(), bucket.getValue());
      Map<LiveDataSpecification, FudgeMsg> normalizerResult = bucket.getKey().normalizeValues(bucket.getValue());
      combinedResults.putAll(normalizerResult);
    }
    return combinedResults;
  }

  //-------------------------------------------------------------------------
  private Normalizer getNormalizer(LiveDataSpecification liveDataSpecification) {
    for (ExternalId externalId : liveDataSpecification.getIdentifiers().getExternalIds()) {
      Normalizer schemeNormalizer = _normalizers.get(externalId.getScheme());
      if (schemeNormalizer != null) {
        return schemeNormalizer;
      }
    }
    return null;
  }
  
  private static ExternalScheme toExternalScheme(final Object o) {
    if (o instanceof ExternalScheme) {
      return (ExternalScheme) o;
    } else if (o instanceof String) {
      return ExternalScheme.of((String) o);
    } else {
      throw new IllegalArgumentException("Bad key - " + o);
    }
  }
  
}
