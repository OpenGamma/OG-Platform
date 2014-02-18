/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.legalentity;

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

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbLegalEntityBeanMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbLegalEntityBeanMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbLegalEntityBeanMasterTest.class);

  private DbLegalEntityBeanMaster _lenMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbLegalEntityBeanMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _lenMaster = new DbLegalEntityBeanMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _lenMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_lenMaster);
    assertEquals(true, _lenMaster.getUniqueIdScheme().equals("DbLen"));
    assertNotNull(_lenMaster.getDbConnector());
    assertNotNull(_lenMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_addAndGet() throws Exception {
    MockLegalEntity len = new MockLegalEntity("London", ExternalIdBundle.of("Test", "OG"), Currency.GBP);
    LegalEntityDocument addDoc = new LegalEntityDocument(len);
    LegalEntityDocument added = _lenMaster.add(addDoc);

    LegalEntityDocument loaded = _lenMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = com.opengamma.DataNotFoundException.class)
  public void test_get_not_existent() throws Exception {
    _lenMaster.get(UniqueId.of("DbLen", "12331"));
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false)
  public void test_concurrentModification() {
    final AtomicReference<Throwable> exceptionOccurred = new AtomicReference<Throwable>();
    Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          test_addAndGet();
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
    assertEquals("DbLegalEntityBeanMaster[DbLen]", _lenMaster.toString());
  }

}
