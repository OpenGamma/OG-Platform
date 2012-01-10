/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * A basic normalization service using other services.
 */
public class DefaultNormalizer implements Normalizer {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultNormalizer.class);
  
  private final NormalizationRuleResolver _ruleResolver;
  private final IdResolver _idResolver;

  public DefaultNormalizer(final IdResolver idResolver, final NormalizationRuleResolver ruleResolver) {
    _idResolver = idResolver;
    _ruleResolver = ruleResolver;
  }

  private IdResolver getIdResolver() {
    return _idResolver;
  }

  private NormalizationRuleResolver getRuleResolver() {
    return _ruleResolver;
  }

  /**
   * @param id the id, not null
   * @param ruleSet the rule set id, not null
   * @param data the values to normalize, not null
   * @return the normalized values, or null if there is a problem
   */
  protected FudgeMsg normalizeValues(final ExternalId id, final String ruleSet, final FudgeMsg data) {
    final NormalizationRuleSet rules = getRuleResolver().resolve(ruleSet);
    if (rules == null) {
      s_logger.warn("No rule set found with ID {}; message will be dropped", ruleSet);
      return null;
    }
    return rules.getNormalizedMessage(data, id.getValue(), new FieldHistoryStore());
  }

  @Override
  public FudgeMsg normalizeValues(final LiveDataSpecification specification, final FudgeMsg data) {
    final ExternalId id = getIdResolver().resolve(specification.getIdentifiers());
    if (id != null) {
      return normalizeValues(id, specification.getNormalizationRuleSetId(), data);
    }
    return null;
  }

  @Override
  public Map<LiveDataSpecification, FudgeMsg> normalizeValues(Map<LiveDataSpecification, ? extends FudgeMsg> data) {
    final Map<ExternalIdBundle, LiveDataSpecification> dataByBundle = Maps.newHashMapWithExpectedSize(data.size());
    for (LiveDataSpecification lds : data.keySet()) {
      dataByBundle.put(lds.getIdentifiers(), lds);
    }
    final Map<LiveDataSpecification, FudgeMsg> result = Maps.newHashMapWithExpectedSize(data.size());
    for (Map.Entry<ExternalIdBundle, ExternalId> id : getIdResolver().resolve(dataByBundle.keySet()).entrySet()) {
      if (id.getValue() != null) {
        final LiveDataSpecification lds = dataByBundle.get(id.getKey());
        final FudgeMsg normalized = normalizeValues(id.getValue(), lds.getNormalizationRuleSetId(), data.get(lds));
        if (normalized != null) {
          result.put(lds, normalized);
        }
      }
    }
    return result;
  }

}
