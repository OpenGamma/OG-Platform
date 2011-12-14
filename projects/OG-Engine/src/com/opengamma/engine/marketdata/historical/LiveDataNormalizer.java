/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.Normalizer;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that normalizes based on the OG-LiveData
 * rule set.
 */
public class LiveDataNormalizer implements HistoricalMarketDataNormalizer {

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

}
