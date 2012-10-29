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
public class CogdaLiveDataSubscriptionResponseBuilder implements FudgeBuilder<CogdaLiveDataSubscriptionResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, CogdaLiveDataSubscriptionResponseMessage response) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.SUBSCRIPTION_RESPONSE.name());
    CogdaLiveDataBuilderUtil.addResponseFields(msg, response);
    msg.add("snapshot", response.getSnapshot());
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CogdaLiveDataSubscriptionResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static CogdaLiveDataSubscriptionResponseMessage buildObjectStatic(FudgeDeserializer deserializer, FudgeMsg message) {
    CogdaLiveDataSubscriptionResponseMessage response = new CogdaLiveDataSubscriptionResponseMessage();
    CogdaLiveDataBuilderUtil.setResponseFields(message, response);
    FudgeMsg snapshot = message.getMessage("snapshot");
    if (snapshot != null) {
      response.setSnapshot(message.getMessage("snapshot"));
    }
    return response;
  }
  
  @Override
  public CogdaLiveDataSubscriptionResponseMessage buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
