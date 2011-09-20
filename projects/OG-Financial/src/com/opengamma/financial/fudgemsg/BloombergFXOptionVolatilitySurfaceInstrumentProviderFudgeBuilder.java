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
public class BloombergFXOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFXOptionVolatilitySurfaceInstrumentProvider> {

  /** Field name. */
  public static final String FX_PREFIX_FIELD_NAME = "FX_PREFIX";
  /** Field name. */
  public static final String POSTFIX_FIELD_NAME = "POSTFIX";
  /** Field name. */
  public static final String DATA_FIELD_NAME = "DATA_FIELD_NAME";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFXOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergFXOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(FX_PREFIX_FIELD_NAME, object.getFXPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergFXOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergFXOptionVolatilitySurfaceInstrumentProvider(message.getString(FX_PREFIX_FIELD_NAME), message.getString(POSTFIX_FIELD_NAME), message.getString(DATA_FIELD_NAME));
  }

}
