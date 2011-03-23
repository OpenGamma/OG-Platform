/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import javax.time.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InMemoryInterpolatedYieldCurveDefinitionMasterTest {

  private InMemoryInterpolatedYieldCurveDefinitionMaster _master;

  @BeforeMethod
  public void init() {
    _master = new InMemoryInterpolatedYieldCurveDefinitionMaster();
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "A")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "1", "B")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "2", "C")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "2", "D")));
  }

  @Test
  public void testGetDefinition () {
    YieldCurveDefinition yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.USD, "2");
    assertNotNull(yc);
    assertEquals("C", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.GBP, "2");
    assertNotNull(yc);
    assertEquals("D", yc.getInterpolatorName());
  }
  
  @Test
  public void testGetDefinition_missing() {
    assertNull(_master.getDefinition(Currency.USD, "3"));
    assertNull(_master.getDefinition(Currency.CHF, "1"));
  }
  
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetDefinition_instant () {
    _master.getDefinition (Currency.USD, "FUNDING", Instant.now ());
  }
  
  /**
   * Force the system clock to advance by at least 1ms.
   */
  private void sleep() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
    }
  }

  @Test
  public void testGetDefinition_versioned () {
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()));
    sleep();
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "3", "E")));
    _master.update(new YieldCurveDefinitionDocument(UniqueIdentifier.of(_master.getIdentifierScheme(), "1_GBP"), new YieldCurveDefinition(Currency.GBP, null, "1", "E")));
    // Expect original data
    YieldCurveDefinition yc = _master.getDefinition(Currency.GBP, "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    Instant nextInstant = Instant.now();
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "3_GBP"));
    // Still at original instant - expect original data
    yc = _master.getDefinition(Currency.GBP, "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(nextInstant));
    // Expect first set of new data
    yc = _master.getDefinition(Currency.GBP, "3");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()));
    // Expect to see the delete
    yc = _master.getDefinition(Currency.GBP, "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdd_duplicate() {
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E")));
  }
  
  @Test
  public void testAddOrUpdate_add () {
    assertNull(_master.getDefinition(Currency.USD, "3"));
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "3", "E")));
    assertNotNull(_master.getDefinition(Currency.USD, "3"));
  }
  
  @Test
  public void testAddOrUpdate_update () {
    YieldCurveDefinition yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E")));
    yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }
  
  @Test
  public void testAddOrUpdate_discardOld () {
    Instant first = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E")));
    Instant second = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "F")));
    // This should only have kept the second one
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    assertNull(_master.getDefinition(Currency.USD, "1"));
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    assertNotNull(_master.getDefinition(Currency.USD, "1"));
  }
  
  @Test(expectedExceptions=UnsupportedOperationException.class)
  public void testCorrect () {
    _master.correct(null);
  }
  
  @Test
  public void testGet () {
    YieldCurveDefinitionDocument ycdoc = _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "1_USD"));
    assertNotNull(ycdoc);
    YieldCurveDefinition yc = ycdoc.getYieldCurveDefinition();
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
  }
  
  @Test(expectedExceptions=DataNotFoundException.class)
  public void testGet_missing () {
    _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "GBP_3"));
  }
  
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGet_deleted () {
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
  }
  
  @Test
  public void testRemove () {
    assertNotNull(_master.getDefinition(Currency.USD, "1"));
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "1_USD"));
    assertNull(_master.getDefinition(Currency.USD, "1"));
  }
  
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemove_missing () {
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "3_USD"));
  }
  
  @Test
  public void testRemove_discardOld () {
    Instant first = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E")));
    Instant second = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "1_USD"));
    // This should only have kept the second one
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    assertNull(_master.getDefinition(Currency.USD, "1"));
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    assertNotNull(_master.getDefinition(Currency.USD, "1"));
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()));
    assertNull(_master.getDefinition(Currency.USD, "1"));
  }
  
  @Test(expectedExceptions=DataNotFoundException.class)
  public void testUpdate_missing () {
    _master.update(new YieldCurveDefinitionDocument(UniqueIdentifier.of(_master.getIdentifierScheme(), "3_USD"), new YieldCurveDefinition(Currency.USD, null, "3", "E")));
  }
  
  @Test
  public void testUpdate_discardOld () {
    Instant first = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E")));
    Instant second = Instant.now();
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "1_USD"));
    // This should only have kept the second one
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(first));
    assertNull(_master.getDefinition(Currency.USD, "1"));
    _master.setVersionCorrection(VersionCorrection.ofVersionAsOf(second));
    assertNotNull(_master.getDefinition(Currency.USD, "1"));
  }
  
}
