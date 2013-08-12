/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.enginedb.stats;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.stats.FunctionCostsDocument;
import com.opengamma.enginedb.stats.DbFunctionCostsMaster;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbFunctionCostsMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbFunctionCostsMasterTest.class);

  private DbFunctionCostsMaster _costsMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbFunctionCostsMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _costsMaster = new DbFunctionCostsMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _costsMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_costsMaster);
    assertNotNull(_costsMaster.getDbConnector());
    assertNotNull(_costsMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_master() throws Exception {
    FunctionCostsDocument doc1 = new FunctionCostsDocument();
    doc1.setConfigurationName("Conf1");
    doc1.setFunctionId("Func1");
    doc1.setInvocationCost(1.1);
    doc1.setDataInputCost(2.2);
    doc1.setDataOutputCost(3.3);
    FunctionCostsDocument doc2 = new FunctionCostsDocument();
    doc2.setConfigurationName("Conf2");
    doc2.setFunctionId("Func2");
    doc2.setInvocationCost(10.1);
    doc2.setDataInputCost(20.2);
    doc2.setDataOutputCost(30.3);
    
    // check empty
    assertNull(_costsMaster.load("Conf1", "Func1", null));
    assertNull(_costsMaster.load("Conf2", "Func2", null));
    
    // store1
    FunctionCostsDocument stored1 = _costsMaster.store(doc1);
    assertNotNull(stored1);
    assertNotNull(stored1.getVersion());
    assertEquals("Conf1", stored1.getConfigurationName());
    assertEquals("Func1", stored1.getFunctionId());
    assertEquals(1.1, stored1.getInvocationCost(), 0.001);
    assertEquals(2.2, stored1.getDataInputCost(), 0.001);
    assertEquals(3.3, stored1.getDataOutputCost(), 0.001);
    
    FunctionCostsDocument loaded1 = _costsMaster.load("Conf1", "Func1", null);
    assertNotSame(stored1, loaded1);
    assertEquals(stored1.getVersion(), loaded1.getVersion());
    assertEquals("Conf1", loaded1.getConfigurationName());
    assertEquals("Func1", loaded1.getFunctionId());
    assertEquals(1.1, loaded1.getInvocationCost(), 0.001);
    assertEquals(2.2, loaded1.getDataInputCost(), 0.001);
    assertEquals(3.3, loaded1.getDataOutputCost(), 0.001);
    
    // store2
    FunctionCostsDocument stored2 = _costsMaster.store(doc2);
    assertNotNull(stored2);
    assertNotNull(stored2.getVersion());
    assertEquals("Conf2", stored2.getConfigurationName());
    assertEquals("Func2", stored2.getFunctionId());
    assertEquals(10.1, stored2.getInvocationCost(), 0.001);
    assertEquals(20.2, stored2.getDataInputCost(), 0.001);
    assertEquals(30.3, stored2.getDataOutputCost(), 0.001);
    
    FunctionCostsDocument loaded2 = _costsMaster.load("Conf2", "Func2", null);
    assertNotSame(stored2, loaded2);
    assertEquals(stored2.getVersion(), loaded2.getVersion());
    assertEquals("Conf2", loaded2.getConfigurationName());
    assertEquals("Func2", loaded2.getFunctionId());
    assertEquals(10.1, loaded2.getInvocationCost(), 0.001);
    assertEquals(20.2, loaded2.getDataInputCost(), 0.001);
    assertEquals(30.3, loaded2.getDataOutputCost(), 0.001);
    
    // safe from external modify
    doc1.setInvocationCost(0);
    FunctionCostsDocument loaded1b = _costsMaster.load("Conf1", "Func1", null);
    assertNotSame(loaded1, loaded1b);
    assertEquals(loaded1, loaded1b);
  }

  @Test
  public void test_history() throws Exception {
    FunctionCostsDocument doc1 = new FunctionCostsDocument();
    doc1.setConfigurationName("Conf");
    doc1.setFunctionId("Func");
    doc1.setInvocationCost(1.1);
    doc1.setDataInputCost(2.2);
    doc1.setDataOutputCost(3.3);
    FunctionCostsDocument doc2 = new FunctionCostsDocument();
    doc2.setConfigurationName("Conf");
    doc2.setFunctionId("Func");
    doc2.setInvocationCost(1.2);
    doc2.setDataInputCost(2.3);
    doc2.setDataOutputCost(3.4);
    
    // check empty
    assertNull(_costsMaster.load("Conf", "Func", null));
    
    // store
    FunctionCostsDocument stored1 = _costsMaster.store(doc1);
    Thread.sleep(50);
    FunctionCostsDocument stored2 = _costsMaster.store(doc2);
    
    // load latest
    FunctionCostsDocument loadedLatest = _costsMaster.load("Conf", "Func", null);
    assertEquals(stored2.getVersion(), loadedLatest.getVersion());
    assertEquals("Conf", loadedLatest.getConfigurationName());
    assertEquals("Func", loadedLatest.getFunctionId());
    assertEquals(1.2, loadedLatest.getInvocationCost(), 0.001);
    assertEquals(2.3, loadedLatest.getDataInputCost(), 0.001);
    assertEquals(3.4, loadedLatest.getDataOutputCost(), 0.001);
    
    // load historic
    FunctionCostsDocument loadedHistoric = _costsMaster.load("Conf", "Func", stored1.getVersion());
    assertEquals(stored1.getVersion(), loadedHistoric.getVersion());
    assertEquals("Conf", loadedHistoric.getConfigurationName());
    assertEquals("Func", loadedHistoric.getFunctionId());
    assertEquals(1.1, loadedHistoric.getInvocationCost(), 0.001);
    assertEquals(2.2, loadedHistoric.getDataInputCost(), 0.001);
    assertEquals(3.3, loadedHistoric.getDataOutputCost(), 0.001);
    FunctionCostsDocument loadedHistoric2 = _costsMaster.load("Conf", "Func", stored1.getVersion().plusMillis(1));
    assertEquals(loadedHistoric, loadedHistoric2);
    
    // load prehistoric
    assertNull(_costsMaster.load("Conf", "Func", stored1.getVersion().minusMillis(1)));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbFunctionCostsMaster", _costsMaster.toString());
  }

}
