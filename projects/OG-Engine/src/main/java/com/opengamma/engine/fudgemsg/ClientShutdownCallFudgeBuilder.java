/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.listener.ClientShutdownCall;

/**
 * Fudge message builder for {@link ClientShutdownCall}. 
 */
@FudgeBuilderFor(ClientShutdownCall.class)
public class ClientShutdownCallFudgeBuilder implements FudgeBuilder<ClientShutdownCall> {

  private static final String METADATA_FIELD = "exception";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ClientShutdownCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, METADATA_FIELD, null, object.getException());
    return msg;
  }

  @Override
  public ClientShutdownCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField exceptionField = msg.getByName(METADATA_FIELD);
    Exception exception = exceptionField != null ? deserializer.fieldValueToObject(Exception.class, exceptionField) : null;
    return new ClientShutdownCall(exception);
  }

}
