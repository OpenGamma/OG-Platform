/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.util.test.DBTest;
import com.opengamma.util.test.HibernateTest;

/**
 * Test HibernateAuditLogger.
 */
public class HibernateAuditLoggerTest {

  @Factory
  public static Object[] factory_tests() {
    Collection<Object[]> tests = DBTest.getParameters();
    Collection<Object> result = new ArrayList<Object>(tests.size());
    for (Object[] objects : tests) {
      result.add(new HibernateAuditLoggerTests(objects[0].toString(), objects[1].toString()));
    }
    return result.toArray();
  }

  @Test
  public static class HibernateAuditLoggerTests extends HibernateTest {

    public HibernateAuditLoggerTests(String databaseType, final String databaseVersion) {
      super(databaseType, databaseVersion);
    }

    @BeforeMethod
    public void setUp() throws Exception {
      super.setUp();
    }

    @AfterMethod
    public void tearDown() throws Exception {
      super.tearDown();
    }

    @Override
    public Class<?>[] getHibernateMappingClasses() {
      return new Class[] { AuditLogEntry.class };
    }  

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
}
