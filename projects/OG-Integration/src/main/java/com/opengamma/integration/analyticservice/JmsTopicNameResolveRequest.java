/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class JmsTopicNameResolveRequest {
  
  private final String _calcConfig;
  
  private final ValueSpecification _valueSpecification;
  
  public JmsTopicNameResolveRequest(final String calcConfig, final ValueSpecification valueSpecification) {
    _calcConfig = calcConfig;
    _valueSpecification = valueSpecification;
  }
  
  /**
   * Gets the calcConfig.
   * @return the calcConfig
   */
  public String getCalcConfig() {
    return _calcConfig;
  }

  /**
   * Gets the valueSpecification.
   * @return the valueSpecification
   */
  public ValueSpecification getValueSpecification() {
    return _valueSpecification;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this); 
  }

}
