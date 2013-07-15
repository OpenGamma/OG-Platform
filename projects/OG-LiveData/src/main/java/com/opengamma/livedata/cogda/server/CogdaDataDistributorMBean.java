/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
@ManagedResource(
    description = "CogdaDataDistributor attributes and operations that can be managed via JMX"
    )
public class CogdaDataDistributorMBean {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaDataDistributorMBean.class);
  private final CogdaDataDistributor _distributor;
  
  public CogdaDataDistributorMBean(CogdaDataDistributor distributor) {
    ArgumentChecker.notNull(distributor, "distributor");
    _distributor = distributor;
  }

  /**
   * Gets the distributor.
   * @return the distributor
   */
  protected CogdaDataDistributor getDistributor() {
    return _distributor;
  }

  @ManagedAttribute(description = "The external identifier scheme for ticks from this distributor.")
  public String getExternalIdScheme() {
    try {
      return getDistributor().getExternalIdScheme();
    } catch (RuntimeException e) {
      s_logger.error("getExternalIdScheme() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

}
