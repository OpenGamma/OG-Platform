/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFuturePriceCurveInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergIRFuturePriceCurveInstrumentProvider.class)
public class BloombergIRFuturePriceCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergIRFuturePriceCurveInstrumentProvider> {

  private static final Logger LOG = LoggerFactory.getLogger(BloombergIRFuturePriceCurveInstrumentProviderFudgeBuilder.class);
  
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergIRFuturePriceCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergIRFuturePriceCurveInstrumentProvider.class);
    message.add("futurePrefix", object.getFuturePrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    
    String scheme = object.getTickerScheme();
    if (scheme == null) {
      scheme = "BLOOMBERG_TICKER_WEAK";
      LOG.warn("{} FuturePriceCurveSpecification field, tickerScheme, was null. Using BLOOMBERG_TICKER_WEAK. Please Update in Configurations by choosing desired tickerScheme and saving.", 
          object.getFuturePrefix());
    }
    message.add("tickerScheme", scheme);
    return message;
  }

  @Override
  public BloombergIRFuturePriceCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    String scheme = message.getString("tickerScheme");
    if (scheme == null) {
      scheme = "BLOOMBERG_TICKER_WEAK";
      LOG.warn("{} FuturePriceCurveSpecification field, tickerScheme, was null. Using BLOOMBERG_TICKER_WEAK. Please Update in Configurations by choosing desired tickerScheme and saving.",
          message.getString("futurePrefix"));
    }
    return new BloombergIRFuturePriceCurveInstrumentProvider(message.getString("futurePrefix"),
                                                             message.getString("postfix"),
                                                             message.getString("dataFieldName"),
                                                             scheme);
  }
}
