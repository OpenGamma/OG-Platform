/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSubscriptionResult;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionResponseMessage implements Serializable {
  private static final String RESULT_FIELD_NAME = "result";
  private static final String USER_MESSAGE_FIELD_NAME = "userMessage";
  private LiveDataSubscriptionResult _subscriptionResult;
  private String _userMessage;

  /**
   * @return the subscriptionResult
   */
  public LiveDataSubscriptionResult getSubscriptionResult() {
    return _subscriptionResult;
  }

  /**
   * @param subscriptionResult the subscriptionResult to set
   */
  public void setSubscriptionResult(LiveDataSubscriptionResult subscriptionResult) {
    _subscriptionResult = subscriptionResult;
  }

  /**
   * @return the userMessage
   */
  public String getUserMessage() {
    return _userMessage;
  }

  /**
   * @param userMessage the userMessage to set
   */
  public void setUserMessage(String userMessage) {
    _userMessage = userMessage;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public FudgeMsg toFudgeMsg(FudgeContext context) {
    FudgeMsg msg = context.newMessage();
    if(getSubscriptionResult() != null) {
      msg.add(RESULT_FIELD_NAME, getSubscriptionResult().name());
    }
    if(getUserMessage() != null) {
      msg.add(USER_MESSAGE_FIELD_NAME, getUserMessage());
    }
    return msg;
  }

  public static SubscriptionResponseMessage fromFudgeMsg(FudgeMsg msg) {
    SubscriptionResponseMessage result = new SubscriptionResponseMessage();
    String subResultText = msg.getString(RESULT_FIELD_NAME);
    if(subResultText != null) {
      result.setSubscriptionResult(LiveDataSubscriptionResult.valueOf(subResultText));
    }
    result.setUserMessage(msg.getString(USER_MESSAGE_FIELD_NAME));
    return result;
  }
}
