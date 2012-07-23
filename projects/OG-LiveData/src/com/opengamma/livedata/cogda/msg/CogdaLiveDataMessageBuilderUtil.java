/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.id.ExternalId;

/**
 * 
 */
public final class CogdaLiveDataMessageBuilderUtil {
  private CogdaLiveDataMessageBuilderUtil() {
  }

  public static void addExternalId(MutableFudgeMsg msg, ExternalId subscriptionId, String normalizationScheme) {
    msg.add("subscriptionIdScheme", subscriptionId.getScheme().getName());
    msg.add("subscriptionIdValue", subscriptionId.getValue());
    if (normalizationScheme != null) {
      msg.add("normalizationScheme", normalizationScheme);
    }
  }
  
  public static void addResponseFields(MutableFudgeMsg msg, CogdaLiveDataCommandResponseMessage response) {
    msg.add("correlationId", response.getClass());
    addExternalId(msg, response.getSubscriptionId(), response.getNormalizationScheme());
    
    msg.add("genericResult", response.getGenericResult().name());
    msg.add("userMessage", response.getUserMessage());
    
  }
  
  

}
