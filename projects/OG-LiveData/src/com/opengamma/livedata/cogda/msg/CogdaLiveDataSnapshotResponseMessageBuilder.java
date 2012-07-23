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
public class CogdaLiveDataSnapshotResponseMessageBuilder implements FudgeMessageBuilder<CogdaLiveDataSnapshotResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, CogdaLiveDataSnapshotResponseMessage response) {
    MutableFudgeMsg msg = serializer.newMessage();
    CogdaLiveDataMessageBuilderUtil.addResponseFields(msg, response);
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CogdaLiveDataSnapshotResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

}
