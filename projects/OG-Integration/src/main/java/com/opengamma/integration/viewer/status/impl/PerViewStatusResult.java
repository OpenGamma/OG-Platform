/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.collect.Maps;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.ImmutableViewStatusKey;
import com.opengamma.util.ArgumentChecker;

/**
 * Per view status result for a security type.
 */
public class PerViewStatusResult {
  
  private final String _securityType;
  private final Map<ViewStatusKey, ViewStatus> _viewStatusResult = Maps.newConcurrentMap();
  
  
  public PerViewStatusResult(String securityType) {
    ArgumentChecker.notNull(securityType, "securityType");
    _securityType = securityType;
  }
    
  public void put(ViewStatusKey key, ViewStatus status) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(status, "status");
    
    _viewStatusResult.put(ImmutableViewStatusKey.of(key), status);
  }
  
  public ViewStatus get(ViewStatusKey key) {
    if (key == null) {
      return null;
    } else {
      return _viewStatusResult.get(ImmutableViewStatusKey.of(key)); 
    }
  }
  
  public Set<ViewStatusKey> keySet() {
    return _viewStatusResult.keySet();
  }
  
  /**
   * Gets the securityType.
   * @return the securityType
   */
  public String getSecurityType() {
    return _securityType;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
