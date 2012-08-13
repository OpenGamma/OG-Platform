/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationFudgeBuilder;

/**
 * 
 */
public class CogdaLiveDataUpdateBeanBuilder implements FudgeBuilder<CogdaLiveDataUpdateBean> {

  public static MutableFudgeMsg buildMessageStatic(FudgeSerializer serializer, CogdaLiveDataUpdateBean update) {
    if (update == null) {
      return null;
    }
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add("LiveDataSpecification", LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, update.getLiveDataSpecification()));
    msg.add("Data", update.getNormalizedFields());
    return msg;
  }
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CogdaLiveDataUpdateBean update) {
    return buildMessageStatic(serializer, update);
  }

  public static CogdaLiveDataUpdateBean buildObjectStatic(FudgeDeserializer deserializer, FudgeMsg message) {
    LiveDataSpecification ldspec = null;
    FudgeMsg ldSpecMsg = message.getMessage("LiveDataSpecification");
    if (ldSpecMsg != null) {
      ldspec = LiveDataSpecificationFudgeBuilder.fromFudgeMsg(deserializer, ldSpecMsg);
    }
    return new CogdaLiveDataUpdateBean(ldspec, message.getMessage("Data"));
  }
  
  @Override
  public CogdaLiveDataUpdateBean buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
