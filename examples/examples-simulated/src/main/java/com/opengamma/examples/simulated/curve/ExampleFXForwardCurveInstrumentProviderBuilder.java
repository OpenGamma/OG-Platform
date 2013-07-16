/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.curve;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;


/**
 * Fudge builder for the example FX forward curve instrument provider.
 */
@FudgeBuilderFor(ExampleFXForwardCurveInstrumentProvider.class)
public class ExampleFXForwardCurveInstrumentProviderBuilder implements FudgeBuilder<ExampleFXForwardCurveInstrumentProvider> {
  /** The data field name */
  private static final String DATA_FIELD_NAME = "dataFieldName";
  /** The postfix */
  private static final String POSTFIX = "postfix";
  /** The prefix */
  private static final String PREFIX = "prefix";
  /** The spot prefix */
  private static final String SPOT_PREFIX = "spotPrefix";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExampleFXForwardCurveInstrumentProvider object) {
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
  public ExampleFXForwardCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX);
    final String postfix = message.getString(POSTFIX);
    String spotPrefix = message.getString(SPOT_PREFIX);
    if (spotPrefix == null) {
      spotPrefix = prefix;
    }
    final String dataFieldName = message.getString(DATA_FIELD_NAME);
    return new ExampleFXForwardCurveInstrumentProvider(prefix, postfix, spotPrefix, dataFieldName);
  }
}
