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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.BondSecuritySearchRequest;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Test DbSecurityMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbSecurityMasterTest extends AbstractDbSecurityTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterTest.class);

  private DbSecurityMaster _secMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _secMaster = new DbSecurityMaster(getDbConnector());
    _secMaster.setDetailProvider(new HibernateSecurityMasterDetailProvider());
  }

  @Override
  protected void doTearDown() {
    _secMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_secMaster);
    assertEquals(true, _secMaster.getUniqueIdScheme().equals("DbSec"));
    assertNotNull(_secMaster.getDbConnector());
    assertNotNull(_secMaster.getClock());
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

  @Test
  public void test_equity_withAttribute() throws Exception {
    EquitySecurity sec = new EquitySecurity("London", "LON", "OpenGamma Ltd", Currency.GBP);
    sec.setName("OpenGamma");
    sec.setGicsCode(GICSCode.of("20102010"));
    sec.setShortName("OG");
    sec.setExternalIdBundle(ExternalIdBundle.of("Test", "OG"));
    sec.addAttribute("ATTR_KEY", "ATTR_VALUE");
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  @Test
  public void test_equity_with_attribute_permission() throws Exception {
    EquitySecurity sec = new EquitySecurity("London", "LON", "OpenGamma Ltd", Currency.GBP);
    sec.setName("OpenGamma");
    sec.setGicsCode(GICSCode.of("20102010"));
    sec.setShortName("OG");
    sec.setExternalIdBundle(ExternalIdBundle.of("Test", "OG"));
    sec.addAttribute("1", "One");
    sec.addAttribute("2", "Two");
    sec.getRequiredPermissions().add("A");
    sec.getRequiredPermissions().add("B");
    SecurityDocument addDoc = new SecurityDocument(sec);

    SecurityDocument added = _secMaster.add(addDoc);
    assertNotNull(added);
    assertNotNull(added.getSecurity());

    assertNotNull(added.getSecurity().getUniqueId());
    final UniqueId addedUniqueId = UniqueId.of(added.getSecurity().getUniqueId().getScheme(),
        added.getSecurity().getUniqueId().getValue(),
        added.getSecurity().getUniqueId().getVersion());
    added.getSecurity().setUniqueId(null);
    assertEquals(sec, added.getSecurity());

    SecurityDocument loaded = _secMaster.get(addedUniqueId);
    assertEquals(addedUniqueId, loaded.getUniqueId());
    added.getSecurity().setUniqueId(addedUniqueId);
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_bond_withSearchByIssuer() throws Exception {
    ZonedDateTime zdt = ZonedDateTime.parse("2011-01-31T12:00Z[Europe/London]");
    GovernmentBondSecurity sec1 = new GovernmentBondSecurity("US TREASURY N/B", "issuerType", "issuerDomicile", "market",
        Currency.GBP, SimpleYieldConvention.US_TREASURY_EQUIVALANT, new Expiry(zdt),
        "couponType", 23.5d, SimpleFrequency.ANNUAL, DayCounts.ACT_ACT_ISDA,
        zdt, zdt, zdt, 129d, 1324d, 12d, 1d, 2d, 3d);
    sec1.addExternalId(ExternalId.of("abc", "def"));
    SecurityDocument added1 = _secMaster.add(new SecurityDocument(sec1));
    GovernmentBondSecurity sec2 = new GovernmentBondSecurity("UK GOVT", "issuerType", "issuerDomicile", "market",
        Currency.GBP, SimpleYieldConvention.US_TREASURY_EQUIVALANT, new Expiry(zdt),
        "couponType", 23.5d, SimpleFrequency.ANNUAL, DayCounts.ACT_ACT_ISDA,
        zdt, zdt, zdt, 129d, 1324d, 12d, 1d, 2d, 3d);
    sec2.addExternalId(ExternalId.of("abc", "def"));
    SecurityDocument added2 = _secMaster.add(new SecurityDocument(sec2));
    
    SecurityDocument loaded1 = _secMaster.get(added1.getUniqueId());
    assertEquals(added1, loaded1);
    
    SecurityDocument loaded2 = _secMaster.get(added2.getUniqueId());
    assertEquals(added2, loaded2);
    
    BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName("*TREASURY*");
    SecuritySearchResult result = _secMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(loaded1, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false)
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
