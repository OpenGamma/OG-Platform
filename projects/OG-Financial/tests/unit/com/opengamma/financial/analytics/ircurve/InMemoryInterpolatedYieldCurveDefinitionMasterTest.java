/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class InMemoryInterpolatedYieldCurveDefinitionMasterTest {
  
  private InMemoryInterpolatedYieldCurveDefinitionMaster _master = new InMemoryInterpolatedYieldCurveDefinitionMaster ();
  
  @Before
  public void init() {
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "A")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("GBP"), "1", "B")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "2", "C")));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("GBP"), "2", "D")));
  }

  @Test
  public void testGetDefinition () {
    YieldCurveDefinition yc = _master.getDefinition(Currency.getInstance("USD"), "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.getInstance("GBP"), "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.getInstance("USD"), "2");
    assertNotNull(yc);
    assertEquals("C", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.getInstance("GBP"), "2");
    assertNotNull(yc);
    assertEquals("D", yc.getInterpolatorName());
  }
  
  @Test
  public void testGetDefinition_missing() {
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "3"));
    assertNull(_master.getDefinition(Currency.getInstance("CHF"), "1"));
  }
  
  @Test(expected = UnsupportedOperationException.class)
  public void testGetDefinition_instant () {
    _master.getDefinition (Currency.getInstance("USD"), "FUNDING", Instant.now ());
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
    _master.setVersionAsOfInstant(Instant.now());
    sleep();
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("GBP"), "3", "E")));
    _master.update(new YieldCurveDefinitionDocument(UniqueIdentifier.of(_master.getIdentifierScheme(), "GBP_1"), new YieldCurveDefinition(Currency.getInstance("GBP"), "1", "E")));
    // Expect original data
    YieldCurveDefinition yc = _master.getDefinition(Currency.getInstance("GBP"), "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.getInstance("GBP"), "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    Instant nextInstant = Instant.now();
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "GBP_3"));
    // Still at original instant - expect original data
    yc = _master.getDefinition(Currency.getInstance("GBP"), "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.getInstance("GBP"), "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    _master.setVersionAsOfInstant(nextInstant);
    // Expect first set of new data
    yc = _master.getDefinition(Currency.getInstance("GBP"), "3");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.getInstance("GBP"), "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    _master.setVersionAsOfInstant(Instant.now());
    // Expect to see the delete
    yc = _master.getDefinition(Currency.getInstance("GBP"), "3");
    assertNull(yc);
    yc = _master.getDefinition(Currency.getInstance("GBP"), "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAdd_duplicate() {
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "E")));
  }
  
  @Test
  public void testAddOrUpdate_add () {
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "3"));
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "3", "E")));
    assertNotNull(_master.getDefinition(Currency.getInstance("USD"), "3"));
  }
  
  @Test
  public void testAddOrUpdate_update () {
    YieldCurveDefinition yc = _master.getDefinition(Currency.getInstance("USD"), "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "E")));
    yc = _master.getDefinition(Currency.getInstance("USD"), "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }
  
  @Test
  public void testAddOrUpdate_discardOld () {
    Instant first = Instant.now();
    _master.setVersionAsOfInstant(first);
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "E")));
    Instant second = Instant.now();
    _master.setVersionAsOfInstant(second);
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "F")));
    // This should only have kept the second one
    _master.setVersionAsOfInstant(first);
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
    _master.setVersionAsOfInstant(second);
    assertNotNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testCorrect () {
    _master.correct(null);
  }
  
  @Test
  public void testGet () {
    YieldCurveDefinitionDocument ycdoc = _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    assertNotNull(ycdoc);
    YieldCurveDefinition yc = ycdoc.getYieldCurveDefinition();
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
  }
  
  @Test(expected=DataNotFoundException.class)
  public void testGet_missing () {
    _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "GBP_3"));
  }
  
  @Test(expected = DataNotFoundException.class)
  public void testGet_deleted () {
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    _master.get(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
  }
  
  @Test
  public void testRemove () {
    assertNotNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
  }
  
  @Test(expected = DataNotFoundException.class)
  public void testRemove_missing () {
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_3"));
  }
  
  @Test
  public void testRemove_discardOld () {
    Instant first = Instant.now();
    _master.setVersionAsOfInstant(first);
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "E")));
    Instant second = Instant.now();
    _master.setVersionAsOfInstant(second);
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    // This should only have kept the second one
    _master.setVersionAsOfInstant(first);
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
    _master.setVersionAsOfInstant(second);
    assertNotNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
    _master.setVersionAsOfInstant(Instant.now());
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
  }
  
  @Test(expected=DataNotFoundException.class)
  public void testUpdate_missing () {
    _master.update(new YieldCurveDefinitionDocument(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_3"), new YieldCurveDefinition(Currency.getInstance("USD"), "3", "E")));
  }
  
  @Test
  public void testUpdate_discardOld () {
    Instant first = Instant.now();
    _master.setVersionAsOfInstant(first);
    sleep();
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.getInstance("USD"), "1", "E")));
    Instant second = Instant.now();
    _master.setVersionAsOfInstant(second);
    sleep();
    _master.remove(UniqueIdentifier.of(_master.getIdentifierScheme(), "USD_1"));
    // This should only have kept the second one
    _master.setVersionAsOfInstant(first);
    assertNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
    _master.setVersionAsOfInstant(second);
    assertNotNull(_master.getDefinition(Currency.getInstance("USD"), "1"));
  }
  
}
