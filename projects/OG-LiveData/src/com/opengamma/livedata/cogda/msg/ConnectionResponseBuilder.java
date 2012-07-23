/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * 
 */
public class ConnectionResponseBuilder implements FudgeBuilder<ConnectionResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, ConnectionResponseMessage response) {
    MutableFudgeMsg msg = serializer.newMessage();
    
    msg.add("result", response.getResult().name());
    
    return msg;
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ConnectionResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static ConnectionResponseMessage buildObjectStatic(FudgeDeserializer deserializer, FudgeMsg message) {
    ConnectionResponseMessage response = new ConnectionResponseMessage();
    response.setResult(ConnectionResult.valueOf(message.getString("result")));
    return response;
  }
  
  @Override
  public ConnectionResponseMessage buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
