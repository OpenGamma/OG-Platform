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
public class CogdaLiveDataUpdateMessageBuilder implements FudgeMessageBuilder<CogdaLiveDataUpdateMessage> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, CogdaLiveDataUpdateMessage update) {
    MutableFudgeMsg msg = serializer.newMessage();
    
    CogdaLiveDataMessageBuilderUtil.addExternalId(msg, update.getSubscriptionId(), update.getNormalizationScheme());
    
    return msg;
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CogdaLiveDataUpdateMessage object) {
    return buildMessageStatic(serializer, object);
  }

}
