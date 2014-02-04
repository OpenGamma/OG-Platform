/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.jms.JmsConnector;

/**
 * Utility class for common db master initialization functionality.
 */
public final class DBMasterComponentUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(DBMasterComponentUtils.class);
  
  private DBMasterComponentUtils() {}
  
  
  /**
   * Tests whether a given jms config is valid, logging an error if not
   * @param classifier the classifier of the master
   * @param cfClazz the component factory of the master
   * @param jmsConnector the jmsConnector
   * @param jmsChangeManagerTopic the jmsChangeManagerTopic
   * @return whether this configuration is valid
   */
  public static boolean isValidJmsConfiguration(String classifier, Class<? extends AbstractComponentFactory> cfClazz, JmsConnector jmsConnector, String jmsChangeManagerTopic) {
    
    boolean valid = jmsConnector != null && jmsChangeManagerTopic != null;
    if (!valid) {
      s_logger.warn("Change management for master enabled in {} (classifier '{}') " + 
             "but not all jms settings present: jmsChangeManagerTopic={}, jmsConnector={}. " + 
             "Will be disabled. Set enableChangeManagement=false to suppress this warning.", 
             cfClazz,
             classifier,
             jmsChangeManagerTopic, 
             jmsConnector);
    }
    return valid;

  }
  
}
