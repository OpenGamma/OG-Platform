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
 * JMX Instrumentation for any {@link CogdaRecordChunker} instance.
 */
@ManagedResource(
    description = "CogdaRecordChunker attributes and operations that can be managed via JMX"
    )
public class CogdaRecordChunkerMBean {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaRecordChunkerMBean.class);
  private final CogdaRecordChunker _chunker;
  
  public CogdaRecordChunkerMBean(CogdaRecordChunker chunker) {
    ArgumentChecker.notNull(chunker, "chunker");
    _chunker = chunker;
  }

  /**
   * Gets the chunker.
   * @return the chunker
   */
  protected CogdaRecordChunker getChunker() {
    return _chunker;
  }

  @ManagedAttribute(description = "How many different symbols the Chunker has seen since inception.")
  public int getNumActiveSymbols() {
    try {
      return getChunker().getNumActiveSymbols();
    } catch (RuntimeException e) {
      s_logger.error("getNumActiveSymbols() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "All discrete symbols seen by the chunker since restart.")
  public Set<String> getAllSymbols() {
    try {
      return getChunker().getAllSymbols();
    } catch (RuntimeException e) {
      s_logger.error("getAllSymbols() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Description of the remote server connection (e.g. hostname/port).")
  public String getRemoteServerConnectionName() {
    try {
      return getChunker().getRemoteServerConnectionName();
    } catch (RuntimeException e) {
      s_logger.error("getRemoteServerConnectionName() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

}
