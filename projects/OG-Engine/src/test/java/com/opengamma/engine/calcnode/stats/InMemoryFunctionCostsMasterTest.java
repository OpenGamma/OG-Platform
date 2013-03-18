/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.stats.FunctionCostsDocument;
import com.opengamma.engine.calcnode.stats.InMemoryFunctionCostsMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the function statistics gatherer.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryFunctionCostsMasterTest {

  public void test_master() {
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
    InMemoryFunctionCostsMaster test = new InMemoryFunctionCostsMaster();
    assertNull(test.load("Conf1", "Func1", null));
    assertNull(test.load("Conf2", "Func2", null));
    
    // store1
    FunctionCostsDocument stored1 = test.store(doc1);
    assertNotNull(stored1);
    assertNotNull(stored1.getVersion());
    assertEquals("Conf1", stored1.getConfigurationName());
    assertEquals("Func1", stored1.getFunctionId());
    assertEquals(1.1, stored1.getInvocationCost(), 0.001);
    assertEquals(2.2, stored1.getDataInputCost(), 0.001);
    assertEquals(3.3, stored1.getDataOutputCost(), 0.001);
    
    FunctionCostsDocument loaded1 = test.load("Conf1", "Func1", null);
    assertNotSame(stored1, loaded1);
    assertEquals(stored1.getVersion(), loaded1.getVersion());
    assertEquals("Conf1", loaded1.getConfigurationName());
    assertEquals("Func1", loaded1.getFunctionId());
    assertEquals(1.1, loaded1.getInvocationCost(), 0.001);
    assertEquals(2.2, loaded1.getDataInputCost(), 0.001);
    assertEquals(3.3, loaded1.getDataOutputCost(), 0.001);
    
    // store2
    FunctionCostsDocument stored2 = test.store(doc2);
    assertNotNull(stored2);
    assertNotNull(stored2.getVersion());
    assertEquals("Conf2", stored2.getConfigurationName());
    assertEquals("Func2", stored2.getFunctionId());
    assertEquals(10.1, stored2.getInvocationCost(), 0.001);
    assertEquals(20.2, stored2.getDataInputCost(), 0.001);
    assertEquals(30.3, stored2.getDataOutputCost(), 0.001);
    
    FunctionCostsDocument loaded2 = test.load("Conf2", "Func2", null);
    assertNotSame(stored2, loaded2);
    assertEquals(stored2.getVersion(), loaded2.getVersion());
    assertEquals("Conf2", loaded2.getConfigurationName());
    assertEquals("Func2", loaded2.getFunctionId());
    assertEquals(10.1, loaded2.getInvocationCost(), 0.001);
    assertEquals(20.2, loaded2.getDataInputCost(), 0.001);
    assertEquals(30.3, loaded2.getDataOutputCost(), 0.001);
    
    // safe from external modify 
    doc1.setInvocationCost(0);
    FunctionCostsDocument loaded1b = test.load("Conf1", "Func1", null);
    assertNotSame(loaded1, loaded1b);
    assertEquals(loaded1, loaded1b);
  }

}
