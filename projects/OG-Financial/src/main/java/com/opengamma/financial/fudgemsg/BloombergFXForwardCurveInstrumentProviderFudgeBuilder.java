/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.fxforwardcurve.BloombergFXForwardCurveInstrumentProvider;

/**
 * Fudge builder for {@link BloombergFXForwardCurveInstrumentProvider}.
 */
@FudgeBuilderFor(BloombergFXForwardCurveInstrumentProvider.class)
public class BloombergFXForwardCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFXForwardCurveInstrumentProvider> {
  /** The data field name field */
  private static final String DATA_FIELD_NAME = "dataFieldName";
  /** The postfix field */
  private static final String POSTFIX = "postfix";
  /** The prefix field */
  private static final String PREFIX = "prefix";
  /** The spot prefix field */
  private static final String SPOT_PREFIX = "spotPrefix";
  /** The field indicating whether to use the BBG spot ticker */
  private static final String USE_SPOT_RATE_FROM_GRAPH = "useSpotRateFromGraph";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFXForwardCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(PREFIX, object.getPrefix());
    message.add(POSTFIX, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(USE_SPOT_RATE_FROM_GRAPH, object.useSpotRateFromGraph());
    if (!object.useSpotRateFromGraph()) {
      if (!object.getSpotPrefix().equals(object.getPrefix())) {
        message.add(SPOT_PREFIX, object.getSpotPrefix());
      }
    }
    return message;
  }

  @Override
  public BloombergFXForwardCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX);
    final String postfix = message.getString(POSTFIX);
    final String dataFieldName = message.getString(DATA_FIELD_NAME);
    boolean useSpotRateFromGraph = false; // backwards compatibility
    if (message.hasField(USE_SPOT_RATE_FROM_GRAPH)) {
      useSpotRateFromGraph = message.getBoolean(USE_SPOT_RATE_FROM_GRAPH);
    }
    if (!useSpotRateFromGraph) {
      String spotPrefix = message.getString(SPOT_PREFIX);
      if (spotPrefix == null) {
        spotPrefix = prefix;
      }
      return new BloombergFXForwardCurveInstrumentProvider(prefix, postfix, spotPrefix, dataFieldName);
    }
    if (message.hasField(SPOT_PREFIX)) {
      throw new IllegalStateException("Spot prefix present in message even though useSpotRateFromGraph field is true");
    }
    return new BloombergFXForwardCurveInstrumentProvider(prefix, postfix, dataFieldName);
  }

}
