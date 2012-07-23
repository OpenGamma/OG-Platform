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
public class ConnectionResponseMessageBuilder implements FudgeMessageBuilder<ConnectionResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, ConnectionResponseMessage response) {
    MutableFudgeMsg msg = serializer.newMessage();
    
    msg.add("result", response.getResult().name());
    
    return msg;
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ConnectionResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

}
