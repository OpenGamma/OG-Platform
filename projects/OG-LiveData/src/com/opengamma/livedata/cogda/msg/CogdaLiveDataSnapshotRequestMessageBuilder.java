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
public class CogdaLiveDataSnapshotRequestMessageBuilder implements FudgeMessageBuilder<CogdaLiveDataSnapshotRequestMessage> {
  
  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, CogdaLiveDataSnapshotRequestMessage request) {
    MutableFudgeMsg msg = serializer.newMessage();
    
    msg.add("correlationId", request.getCorrelationId());
    CogdaLiveDataMessageBuilderUtil.addExternalId(msg, request.getSubscriptionId(), request.getNormalizationScheme());
    
    return msg;
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CogdaLiveDataSnapshotRequestMessage object) {
    return buildMessageStatic(serializer, object);
  }

}
