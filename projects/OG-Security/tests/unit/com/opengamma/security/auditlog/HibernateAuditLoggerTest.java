/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.net.InetAddress;
import java.util.Collection;

import org.junit.Test;

import com.opengamma.util.test.HibernateTest;

/**
 *
 * 
 */
public class HibernateAuditLoggerTest extends HibernateTest {
  
  public HibernateAuditLoggerTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class[] { AuditLogEntry.class };
  }  
  
  @Test
  public void testLogging() throws Exception {
    HibernateAuditLogger logger = new HibernateAuditLogger(5, 1);
    logger.setSessionFactory(getSessionFactory());
    
    Collection<AuditLogEntry> logEntries = logger.findAll();
    assertEquals(0, logEntries.size()); 
    
    logger.log("jake", "/Portfolio/XYZ123", "View", true);
    logger.log("jake", "/Portfolio/XYZ345", "Modify", "User has no Modify permission on this portfolio", false);
    
    logger.flushCache();
    logger.flushCache();
    
    logEntries = logger.findAll();
    assertEquals(2, logEntries.size()); 
    
    for (AuditLogEntry entry : logEntries) {
      assertEquals("jake", entry.getUser());
      assertEquals(InetAddress.getLocalHost().getHostName(), entry.getOriginatingSystem());

      if (entry.getObject().equals("/Portfolio/XYZ123")) {
        assertEquals("View", entry.getOperation());
        assertNull(entry.getDescription());
        assertTrue(entry.isSuccess());
      
      } else if (entry.getObject().equals("/Portfolio/XYZ345")) {
        assertEquals("Modify", entry.getOperation());
        assertEquals("User has no Modify permission on this portfolio", entry.getDescription());
        assertFalse(entry.isSuccess());
      
      } else {
        fail("Unexpected object ID");
      }
    }
    
  }
}
