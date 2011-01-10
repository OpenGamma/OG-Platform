/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 *
 * @author pietari
 */
public class DuplicateFilteringAuditLoggerTest {
  
  @Test
  public void testDuplicateFiltering() throws InterruptedException {
    InMemoryAuditLogger memoryLogger = new InMemoryAuditLogger();  
    DuplicateFilteringAuditLogger filteringLogger = new DuplicateFilteringAuditLogger(memoryLogger, 1000, 1);
    
    log5Messages(filteringLogger);
    assertEquals(5, memoryLogger.getMessages().size());
    
    log5Messages(filteringLogger);
    assertEquals(5, memoryLogger.getMessages().size());
    
    Thread.sleep(1500);
    
    log5Messages(filteringLogger);
    assertEquals(10, memoryLogger.getMessages().size());
    
    log5Messages(filteringLogger);
    assertEquals(10, memoryLogger.getMessages().size());
  }

  private void log5Messages(DuplicateFilteringAuditLogger filteringLogger) {
    filteringLogger.log("alice", "obj1", "op1", true);
    filteringLogger.log("lisa", "obj1", "op1", true);
    filteringLogger.log("alice", "obj2", "op1", true);
    filteringLogger.log("alice", "obj1", "op2", true);
    filteringLogger.log("alice", "obj1", "op1", false);
  }

}
