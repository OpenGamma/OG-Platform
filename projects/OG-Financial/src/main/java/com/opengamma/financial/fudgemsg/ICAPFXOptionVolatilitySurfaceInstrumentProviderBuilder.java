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

import com.opengamma.financial.analytics.volatility.surface.ICAPFXOptionVolatilitySurfaceInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(ICAPFXOptionVolatilitySurfaceInstrumentProvider.class)
public class ICAPFXOptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<ICAPFXOptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ICAPFXOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("fxPrefix", object.getFXPrefix());
    message.add("ccyPair", object.getCurrencyPair());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public ICAPFXOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String fxPrefix = message.getString("fxPrefix");
    final String ccyPair = message.getString("ccyPair");
    final String dataFieldName = message.getString("dataFieldName");
    return new ICAPFXOptionVolatilitySurfaceInstrumentProvider(fxPrefix, ccyPair, dataFieldName);
  }

}
