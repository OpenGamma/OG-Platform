/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;

/**
 * 
 */
public final class CogdaLiveDataBuilderUtil {
  private CogdaLiveDataBuilderUtil() {
  }

  public static void addExternalId(MutableFudgeMsg msg, ExternalId subscriptionId, String normalizationScheme) {
    msg.add("subscriptionIdScheme", subscriptionId.getScheme().getName());
    msg.add("subscriptionIdValue", subscriptionId.getValue());
    if (normalizationScheme != null) {
      msg.add("normalizationScheme", normalizationScheme);
    }
  }
  
  public static void addResponseFields(MutableFudgeMsg msg, CogdaLiveDataCommandResponseMessage response) {
    msg.add("correlationId", response.getCorrelationId());
    addExternalId(msg, response.getSubscriptionId(), response.getNormalizationScheme());
    
    msg.add("genericResult", response.getGenericResult().name());
    msg.add("userMessage", response.getUserMessage());
    
  }
  
  public static ExternalId parseExternalId(FudgeMsg msg) {
    ExternalId externalId = ExternalId.of(msg.getString("subscriptionIdScheme"), msg.getString("subscriptionIdValue"));
    return externalId;
  }
  
  public static void setResponseFields(FudgeMsg msg, CogdaLiveDataCommandResponseMessage response) {
    if (!msg.hasField("correlationId")) {
      response.setCorrelationId(-1L);
    } else {
      response.setCorrelationId(msg.getLong("correlationId"));
    }
    response.setSubscriptionId(parseExternalId(msg));
    response.setNormalizationScheme(msg.getString("normalizationScheme"));
    response.setGenericResult(CogdaCommandResponseResult.valueOf(msg.getString("genericResult")));
    response.setUserMessage(msg.getString("userMessage"));
  }
  
  public static FudgeMsg buildCommandResponseMessage(FudgeContext fudgeContext, CogdaLiveDataCommandResponseMessage responseMessage) {
    if (responseMessage instanceof CogdaLiveDataSubscriptionResponseMessage) {
      return CogdaLiveDataSubscriptionResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), (CogdaLiveDataSubscriptionResponseMessage) responseMessage);
    } else if (responseMessage instanceof CogdaLiveDataSnapshotResponseMessage) {
      return CogdaLiveDataSnapshotResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), (CogdaLiveDataSnapshotResponseMessage) responseMessage);
    }
    return null;
  }
  
}
