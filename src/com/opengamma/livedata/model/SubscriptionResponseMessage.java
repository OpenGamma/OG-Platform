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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.LiveDataSubscriptionResult;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionResponseMessage implements Serializable {
  private static final String RESULT_FIELD_NAME = "result";
  private static final String USER_MESSAGE_FIELD_NAME = "userMessage";
  private static final String TICK_DISTRIBUTION_FIELD_NAME = "tickDistributionSpecification";
  private LiveDataSubscriptionResult _subscriptionResult;
  private String _userMessage;
  private String _tickDistributionSpecification;
  
  public SubscriptionResponseMessage() {
  }
  
  public SubscriptionResponseMessage(LiveDataSubscriptionResult subscriptionResult) {
    setSubscriptionResult(subscriptionResult);
  }

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

  /**
   * @return the tickDistributionSpecification
   */
  public String getTickDistributionSpecification() {
    return _tickDistributionSpecification;
  }

  /**
   * @param tickDistributionSpecification the tickDistributionSpecification to set
   */
  public void setTickDistributionSpecification(
      String tickDistributionSpecification) {
    _tickDistributionSpecification = tickDistributionSpecification;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if(getSubscriptionResult() != null) {
      msg.add(RESULT_FIELD_NAME, getSubscriptionResult().name());
    }
    if(getUserMessage() != null) {
      msg.add(USER_MESSAGE_FIELD_NAME, getUserMessage());
    }
    if(getTickDistributionSpecification() != null) {
      msg.add(TICK_DISTRIBUTION_FIELD_NAME, getTickDistributionSpecification());
    }
    return msg;
  }

  public static SubscriptionResponseMessage fromFudgeMsg(FudgeFieldContainer msg) {
    SubscriptionResponseMessage result = new SubscriptionResponseMessage();
    String subResultText = msg.getString(RESULT_FIELD_NAME);
    if(subResultText != null) {
      result.setSubscriptionResult(LiveDataSubscriptionResult.valueOf(subResultText));
    }
    result.setUserMessage(msg.getString(USER_MESSAGE_FIELD_NAME));
    result.setTickDistributionSpecification(msg.getString(TICK_DISTRIBUTION_FIELD_NAME));
    return result;
  }
}
