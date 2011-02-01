/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.time.calendar.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.time.Expiry;

/**
 * Test DbSecurityMaster.
 */
public class DbSecurityMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterTest.class);

  private DbSecurityMaster _secMaster;

  public DbSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _secMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_secMaster);
    assertEquals(true, _secMaster.getIdentifierScheme().equals("DbSec"));
    assertNotNull(_secMaster.getDbSource());
    assertNotNull(_secMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equity() throws Exception {
    EquitySecurity sec = new EquitySecurity("London", "LON", "OpenGamma Ltd", Currency.getInstance("GBP"));
    sec.setName("OpenGamma");
    sec.setGicsCode(GICSCode.getInstance(2));
    sec.setShortName("OG");
    sec.setIdentifiers(IdentifierBundle.of(Identifier.of("Test", "OG")));
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_bond() throws Exception {
    ZonedDateTime zdt = ZonedDateTime.parse("2011-01-31T12:00Z[Europe/London]");
    DateTimeWithZone dtwz = new DateTimeWithZone(zdt, zdt.getZone().getID());
    GovernmentBondSecurity sec = new GovernmentBondSecurity("US TREASURY N/B", "issuerType", "issuerDomicile", "market",
        Currency.getInstance("GBP"), SimpleYieldConvention.US_TREASURY_EQUIVALANT, new Expiry(zdt),
        "couponType", 23.5d, SimpleFrequency.ANNUAL, DayCountFactory.INSTANCE.getDayCount("Act/Act"),
        dtwz, dtwz, dtwz, 129d, 1324d, 12d, 1d, 2d, 3d);
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
    
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setBondIssuerName("*TREASURY*");
    SecuritySearchResult result = _secMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(loaded, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Ignore("Test fails because of a known issue")
  @Test
  public void test_concurrentModification() {    
    final AtomicBoolean exceptionOccurred = new AtomicBoolean();
    Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          test_equity();
        } catch (Throwable t) {
          exceptionOccurred.set(true);
          s_logger.error("Error running task", t);
        }
      }
    };
    
    // 5 threads for plenty of concurrent activity
    ExecutorService executor = Executors.newFixedThreadPool(5);
    
    // 10 security inserts is always enough to produce a duplicate key exception
    LinkedList<Future<?>> futures = new LinkedList<Future<?>>();
    for (int i = 0; i < 10; i++) {
      futures.add(executor.submit(task));
    }
    
    while (!futures.isEmpty()) {
      Future<?> future = futures.poll();
      try {
        future.get();
      } catch (Throwable t) {
        s_logger.error("Exception waiting for task to complete", t);
      }
    }
    
    assertFalse(exceptionOccurred.get());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbSecurityMaster[DbSec]", _secMaster.toString());
  }

}
