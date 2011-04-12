/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link RestTarget}
 */
public class RestTargetFactoryBean extends SingletonFactoryBean<RestTarget> {

  private FudgeContext _fudgeContext;
  private EndPointDescriptionProvider _endPointDescriptionProvider;
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public EndPointDescriptionProvider getEndPointDescriptionProvider() {
    return _endPointDescriptionProvider;
  }
  
  public void setEndPointDescriptionProvider(EndPointDescriptionProvider provider) {
    _endPointDescriptionProvider = provider;
  }
  
  @Override
  protected RestTarget createObject() {
    if (getFudgeContext() == null) {
      throw new IllegalArgumentException("fudgeContext must be set");
    }
    if (getEndPointDescriptionProvider() == null) {
      throw new IllegalArgumentException("endPointDescriptionProvider must be set");
    }
    return new RestTarget(getFudgeContext(), getEndPointDescriptionProvider());
  }

}
