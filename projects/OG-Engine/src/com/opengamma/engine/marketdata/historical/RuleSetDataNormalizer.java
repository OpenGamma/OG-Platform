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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that normalizes based on the OG-LiveData
 * rule set.
 */
public class RuleSetDataNormalizer implements HistoricalMarketDataNormalizer {

  private final FudgeContext _fudgeContext;
  private final IdResolver _resolver;
  private final NormalizationRuleSet _rules;

  public RuleSetDataNormalizer(final FudgeContext fudgeContext, final IdResolver resolver, final NormalizationRuleSet rules) {
    _fudgeContext = fudgeContext;
    _resolver = resolver;
    _rules = rules;
  }

  private FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  private IdResolver getResolver() {
    return _resolver;
  }
  
  private NormalizationRuleSet getRules() {
    return _rules;
  }

  @Override
  public Object normalize(final ExternalId identifier, final String name, final Object value) {
    final ExternalId preferredIdentifier = getResolver().resolve(ExternalIdBundle.of(identifier));
    if (preferredIdentifier == null) {
      return null;
    }
    // Note Live Data normalization works at the underlying Fudge message level, so we create a pretend message. Values should
    // be primitives; but if structured data is coming from time series we can attempt to handle it with a serializer.
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    final FudgeFieldType type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if (type != null) {
      msg.add(name, null, type, value);
    } else {
      new FudgeSerializer(getFudgeContext()).addToMessageWithClassHeaders(msg, name, null, value);
    }
    final FudgeMsg newMsg = getRules().getNormalizedMessage(msg, preferredIdentifier.getValue(), new FieldHistoryStore());
    if (newMsg == null) {
      return null;
    }
    return newMsg.getValue(name);
  }

}
