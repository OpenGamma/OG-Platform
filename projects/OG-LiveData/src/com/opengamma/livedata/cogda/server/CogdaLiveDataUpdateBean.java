/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * The internal update object that passes between the
 * {@link CogdaDataDistributor} and {@link CogdaLiveDataServer}.
 */
public class CogdaLiveDataUpdateBean {
  // LiveDataSpecification ldspec, FudgeMsg normalizedFields
  private final LiveDataSpecification _liveDataSpecification;
  private final FudgeMsg _normalizedFields;
  
  public CogdaLiveDataUpdateBean(LiveDataSpecification liveDataSpecification, FudgeMsg normalizedFields) {
    _liveDataSpecification = liveDataSpecification;
    _normalizedFields = normalizedFields;
  }

  /**
   * Gets the liveDataSpecification.
   * @return the liveDataSpecification
   */
  public LiveDataSpecification getLiveDataSpecification() {
    return _liveDataSpecification;
  }

  /**
   * Gets the normalizedFields.
   * @return the normalizedFields
   */
  public FudgeMsg getNormalizedFields() {
    return _normalizedFields;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
