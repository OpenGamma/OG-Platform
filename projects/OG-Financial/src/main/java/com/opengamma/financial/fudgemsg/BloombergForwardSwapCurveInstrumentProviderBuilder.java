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

import com.opengamma.financial.analytics.forwardcurve.BloombergForwardSwapCurveInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergForwardSwapCurveInstrumentProvider.class)
public class BloombergForwardSwapCurveInstrumentProviderBuilder implements FudgeBuilder<BloombergForwardSwapCurveInstrumentProvider> {
  private static final String DATA_FIELD_NAME = "dataFieldName";
  private static final String POSTFIX = "postfix";
  private static final String PREFIX = "prefix";
  private static final String SPOT_PREFIX = "spotPrefix";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergForwardSwapCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(PREFIX, object.getPrefix());
    message.add(POSTFIX, object.getPostfix());
    if (!object.getSpotPrefix().equals(object.getPrefix())) {
      message.add(SPOT_PREFIX, object.getSpotPrefix());
    }
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergForwardSwapCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX);
    final String postfix = message.getString(POSTFIX);
    String spotPrefix = message.getString(SPOT_PREFIX);
    if (spotPrefix == null) {
      spotPrefix = prefix;
    }
    final String dataFieldName = message.getString(DATA_FIELD_NAME);
    return new BloombergForwardSwapCurveInstrumentProvider(prefix, postfix, spotPrefix, dataFieldName);
  }

}
