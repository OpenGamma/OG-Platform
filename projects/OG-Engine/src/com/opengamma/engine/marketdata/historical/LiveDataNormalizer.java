/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.Normalizer;
import com.opengamma.util.tuple.Pair;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that normalizes based on the OG-LiveData
 * rule set.
 */
public class LiveDataNormalizer implements HistoricalMarketDataNormalizer {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataNormalizer.class);

  private final FudgeContext _fudgeContext;
  private final Normalizer _underlying;
  private final String _ruleSetId;

  public LiveDataNormalizer(final FudgeContext fudgeContext, final Normalizer underlying, final String ruleSetId) {
    _fudgeContext = fudgeContext;
    _underlying = underlying;
    _ruleSetId = ruleSetId;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected Normalizer getUnderlying() {
    return _underlying;
  }

  protected String getRuleSetId() {
    return _ruleSetId;
  }

  @Override
  public Object normalize(final ExternalIdBundle identifiers, final String name, final Object value) {
    // Note Live Data normalization works at the underlying Fudge message level, so we create a pretend message. Values should
    // be primitives; but if structured data is coming from time series we can attempt to handle it with a serializer.
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    final FudgeFieldType type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if (type != null) {
      msg.add(name, null, type, value);
    } else {
      new FudgeSerializer(getFudgeContext()).addToMessageWithClassHeaders(msg, name, null, value);
    }
    final FudgeMsg newMsg = getUnderlying().normalizeValues(new LiveDataSpecification(getRuleSetId(), identifiers), msg);
    if (newMsg == null) {
      return null;
    }
    return newMsg.getValue(name);
  }

  @Override
  public Map<Pair<ExternalIdBundle, String>, Object> normalize(final Map<Pair<ExternalIdBundle, String>, Object> values) {
    final Map<LiveDataSpecification, MutableFudgeMsg> request = new HashMap<LiveDataSpecification, MutableFudgeMsg>();
    final Map<Pair<ExternalIdBundle, String>, Object> result = Maps.newHashMapWithExpectedSize(values.size());
    final FudgeTypeDictionary dictionary = getFudgeContext().getTypeDictionary();
    FudgeSerializer serializer = null;
    for (Map.Entry<Pair<ExternalIdBundle, String>, Object> value : values.entrySet()) {
      LiveDataSpecification key = (LiveDataSpecification) result.get(value.getKey());
      if (key == null) {
        key = new LiveDataSpecification(getRuleSetId(), value.getKey().getFirst());
        result.put(value.getKey(), key);
      }
      MutableFudgeMsg msg = request.get(key);
      if (msg == null) {
        msg = getFudgeContext().newMessage();
        request.put(key, msg);
      }
      final FudgeFieldType type = dictionary.getByJavaType(value.getValue().getClass());
      if (type != null) {
        msg.add(value.getKey().getSecond(), null, type, value.getValue());
      } else {
        if (serializer == null) {
          serializer = new FudgeSerializer(getFudgeContext());
        }
        serializer.addToMessageWithClassHeaders(msg, value.getKey().getSecond(), null, value);
      }
    }
    s_logger.debug("Sending {} to {}", request, getUnderlying());
    final Map<LiveDataSpecification, FudgeMsg> response = getUnderlying().normalizeValues(request);
    if (response == null) {
      return Collections.emptyMap();
    }
    for (Map.Entry<Pair<ExternalIdBundle, String>, Object> value : values.entrySet()) {
      final LiveDataSpecification key = (LiveDataSpecification) result.remove(value.getKey());
      final FudgeMsg responseResult = response.get(key);
      if (responseResult != null) {
        final Object normalized = responseResult.getValue(value.getKey().getSecond());
        if (normalized != null) {
          result.put(value.getKey(), normalized);
        }
      }
    }
    return result;
  }

}
