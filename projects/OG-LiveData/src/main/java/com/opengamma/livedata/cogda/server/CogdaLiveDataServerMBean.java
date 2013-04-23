/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
@ManagedResource(
    description = "CogdaLiveDataServer attributes and operations that can be managed via JMX"
    )
public class CogdaLiveDataServerMBean {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaLiveDataServerMBean.class);
  private final CogdaLiveDataServer _server; 
  
  public CogdaLiveDataServerMBean(CogdaLiveDataServer server) {
    ArgumentChecker.notNull(server, "server");
    _server = server;
  }

  /**
   * Gets the server.
   * @return the server
   */
  protected CogdaLiveDataServer getServer() {
    return _server;
  }

  @ManagedAttribute(description = "Active port number.")
  public int getPortNumber() {
    try {
      return getServer().getPortNumber();
    } catch (RuntimeException e) {
      s_logger.error("getPortNumber() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Whether the server is running.")
  public boolean isRunning() {
    try {
      return getServer().isRunning();
    } catch (RuntimeException e) {
      s_logger.error("isRunning() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Current number of client connections.")
  public int getNumClients() {
    try {
      return getServer().getNumClients();
    } catch (RuntimeException e) {
      s_logger.error("getNumClients() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Names of all actively connected users.")
  public Set<String> getActiveUsers() {
    try {
      return getServer().getActiveUsers();
    } catch (RuntimeException e) {
      s_logger.error("getActiveUsers() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

}
