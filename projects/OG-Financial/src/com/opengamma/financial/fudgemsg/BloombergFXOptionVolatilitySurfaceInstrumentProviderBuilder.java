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

import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergFXOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergFXOptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<BloombergFXOptionVolatilitySurfaceInstrumentProvider> {
  private static final String FX_PREFIX = "FX_PREFIX";
  private static final String POSTFIX = "POSTFIX";
  private static final String DATA_FIELD_NAME = "DATA_FIELD_NAME";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFXOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergFXOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(FX_PREFIX, object.getFXPrefix());
    message.add(POSTFIX, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergFXOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergFXOptionVolatilitySurfaceInstrumentProvider(message.getString(FX_PREFIX), message.getString(POSTFIX), message.getString(DATA_FIELD_NAME));
  }

}
