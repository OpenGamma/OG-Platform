/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * 
 */
public class ConnectionRequestMessageBuilder implements FudgeMessageBuilder<ConnectionRequestMessage> {
  
  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, ConnectionRequestMessage request) {
    MutableFudgeMsg msg = serializer.newMessage();
    
    msg.add("userName", request.getUserName());
    
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ConnectionRequestMessage object) {
    return buildMessageStatic(serializer, object);
  }

}
