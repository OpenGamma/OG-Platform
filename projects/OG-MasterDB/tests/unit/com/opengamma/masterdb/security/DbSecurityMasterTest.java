/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.BondSecuritySearchRequest;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.time.Expiry;

/**
 * Test DbSecurityMaster.
 */
public class DbSecurityMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterTest.class);

  private DbSecurityMaster _secMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _secMaster = null;
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_secMaster);
    assertEquals(true, _secMaster.getUniqueIdScheme().equals("DbSec"));
    assertNotNull(_secMaster.getDbConnector());
    assertNotNull(_secMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equity() throws Exception {
    EquitySecurity sec = new EquitySecurity("London", "LON", "OpenGamma Ltd", Currency.GBP);
    sec.setName("OpenGamma");
    sec.setGicsCode(GICSCode.of("20102010"));
    sec.setShortName("OG");
    sec.setExternalIdBundle(ExternalIdBundle.of("Test", "OG"));
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_bond() throws Exception {
    ZonedDateTime zdt = ZonedDateTime.parse("2011-01-31T12:00Z[Europe/London]");
    GovernmentBondSecurity sec = new GovernmentBondSecurity("US TREASURY N/B", "issuerType", "issuerDomicile", "market",
        Currency.GBP, SimpleYieldConvention.US_TREASURY_EQUIVALANT, new Expiry(zdt),
        "couponType", 23.5d, SimpleFrequency.ANNUAL, DayCountFactory.INSTANCE.getDayCount("Act/Act"),
        zdt, zdt, zdt, 129d, 1324d, 12d, 1d, 2d, 3d);
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
    
    BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName("*TREASURY*");
    SecuritySearchResult result = _secMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(loaded, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_concurrentModification() {    
    final AtomicReference<Throwable> exceptionOccurred = new AtomicReference<Throwable>();
    Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          test_equity();
        } catch (Throwable th) {
          exceptionOccurred.compareAndSet(null, th);
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
    
    assertEquals(null, exceptionOccurred.get());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbSecurityMaster[DbSec]", _secMaster.toString());
  }

}
