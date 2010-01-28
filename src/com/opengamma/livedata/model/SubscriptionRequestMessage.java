/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationImpl;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionRequestMessage implements Serializable {
  private static final String USER_NAME_FIELD_NAME = "userName";
  private static final String SPECIFICATION_FIELD_NAME = "specification";
  private String _userName;
  private LiveDataSpecification _specification;
  /**
   * @return the userName
   */
  public String getUserName() {
    return _userName;
  }
  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    _userName = userName;
  }
  /**
   * @return the specification
   */
  public LiveDataSpecification getSpecification() {
    return _specification;
  }
  /**
   * @param specification the specification to set
   */
  public void setSpecification(LiveDataSpecification specification) {
    _specification = specification;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory messageFactory) {
    MutableFudgeFieldContainer msg = messageFactory.newMessage();
    if(getUserName() != null) {
      msg.add(USER_NAME_FIELD_NAME, getUserName());
    }
    if(getSpecification() != null) {
      msg.add(SPECIFICATION_FIELD_NAME, getSpecification().toFudgeMsg(messageFactory));
    }
    return msg;
  }

  public static SubscriptionRequestMessage fromFudgeMsg(FudgeFieldContainer msg) {
    SubscriptionRequestMessage request = new SubscriptionRequestMessage();
    request.setUserName(msg.getString(USER_NAME_FIELD_NAME));
    FudgeFieldContainer specMessage = null;
    FudgeField specField = msg.getByName(SPECIFICATION_FIELD_NAME);
    if((specField != null) && (specField.getValue() instanceof FudgeFieldContainer)) {
      specMessage = (FudgeFieldContainer) specField.getValue();
    }
    if(specMessage != null) {
      request.setSpecification(new LiveDataSpecificationImpl(specMessage));
    }
    return request;
  }
}
