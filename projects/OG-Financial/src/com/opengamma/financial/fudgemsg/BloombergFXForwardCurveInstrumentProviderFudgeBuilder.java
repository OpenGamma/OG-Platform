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
 * 
 */
@FudgeBuilderFor(BloombergFXForwardCurveInstrumentProvider.class)
public class BloombergFXForwardCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFXForwardCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFXForwardCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("prefix", object.getPrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergFXForwardCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString("prefix");
    final String postfix = message.getString("postfix");
    final String dataFieldName = message.getString("dataFieldName");
    return new BloombergFXForwardCurveInstrumentProvider(prefix, postfix, dataFieldName);
  }

}
