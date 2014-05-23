/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryInterpolatedYieldCurveDefinitionMasterTest {

  private InMemoryInterpolatedYieldCurveDefinitionMaster _master;

  @BeforeMethod
  public void init() {
    _master = new InMemoryInterpolatedYieldCurveDefinitionMaster();
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "A", "L1", "R1", true)));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "1", "B", "L2", "R2", false)));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "2", "C", "L3", "R3", false)));
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "2", "D", "L4", "R4", true)));
  }

  @Test
  public void testGetDefinition() {
    YieldCurveDefinition yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    assertEquals("L1", yc.getLeftExtrapolatorName());
    assertEquals("R1", yc.getRightExtrapolatorName());
    assertTrue(yc.isInterpolateYields());
    yc = _master.getDefinition(Currency.GBP, "1");
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    assertEquals("L2", yc.getLeftExtrapolatorName());
    assertEquals("R2", yc.getRightExtrapolatorName());
    assertFalse(yc.isInterpolateYields());
    yc = _master.getDefinition(Currency.USD, "2");
    assertNotNull(yc);
    assertEquals("C", yc.getInterpolatorName());
    assertEquals("L3", yc.getLeftExtrapolatorName());
    assertEquals("R3", yc.getRightExtrapolatorName());
    assertFalse(yc.isInterpolateYields());
    yc = _master.getDefinition(Currency.GBP, "2");
    assertNotNull(yc);
    assertEquals("D", yc.getInterpolatorName());
    assertEquals("L4", yc.getLeftExtrapolatorName());
    assertEquals("R4", yc.getRightExtrapolatorName());
    assertTrue(yc.isInterpolateYields());
  }

  @Test
  public void testGetDefinition_missing() {
    assertNull(_master.getDefinition(Currency.USD, "3"));
    assertNull(_master.getDefinition(Currency.CHF, "1"));
  }

  @Test
  public void testGetDefinition_instant() {
    assertNotNull(_master.getDefinition(Currency.USD, "1", VersionCorrection.ofVersionAsOf(Instant.now())));
  }

  /**
   * Force the system clock to advance by at least 1ms.
   */
  private void sleep() {
    try {
      Thread.sleep(10);
    } catch (final InterruptedException e) {
    }
  }

  @Test
  public void testGetDefinition_versioned() {
    VersionCorrection vc = VersionCorrection.ofVersionAsOf(Instant.now());
    sleep();
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.GBP, null, "3", "E", "L", "R", true)));
    _master.update(new YieldCurveDefinitionDocument(UniqueId.of(_master.getUniqueIdScheme(), "1_GBP"), new YieldCurveDefinition(Currency.GBP, null, "1", "E", "L", "R", false)));
    // Expect original data
    YieldCurveDefinition yc = _master.getDefinition(Currency.GBP, "3", vc);
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1", vc);
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    final Instant nextInstant = Instant.now();
    sleep();
    _master.remove(UniqueId.of(_master.getUniqueIdScheme(), "3_GBP"));
    // Still at original instant - expect original data
    yc = _master.getDefinition(Currency.GBP, "3", vc);
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1", vc);
    assertNotNull(yc);
    assertEquals("B", yc.getInterpolatorName());
    vc = VersionCorrection.ofVersionAsOf(nextInstant);
    // Expect first set of new data
    yc = _master.getDefinition(Currency.GBP, "3", vc);
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    yc = _master.getDefinition(Currency.GBP, "1", vc);
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
    vc = VersionCorrection.ofVersionAsOf(Instant.now());
    // Expect to see the delete
    yc = _master.getDefinition(Currency.GBP, "3", vc);
    assertNull(yc);
    yc = _master.getDefinition(Currency.GBP, "1", vc);
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdd_duplicate() {
    _master.add(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E", "L1", "R1", true)));
  }

  @Test
  public void testAddOrUpdate_add() {
    assertNull(_master.getDefinition(Currency.USD, "3"));
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "3", "E", "L1", "R1", true)));
    assertNotNull(_master.getDefinition(Currency.USD, "3"));
  }

  @Test
  public void testAddOrUpdate_update() {
    YieldCurveDefinition yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
    _master.addOrUpdate(new YieldCurveDefinitionDocument(new YieldCurveDefinition(Currency.USD, null, "1", "E", "L1", "R1", true)));
    yc = _master.getDefinition(Currency.USD, "1");
    assertNotNull(yc);
    assertEquals("E", yc.getInterpolatorName());
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCorrect() {
    _master.correct(null);
  }

  @Test
  public void testGet() {
    final YieldCurveDefinitionDocument ycdoc = _master.get(UniqueId.of(_master.getUniqueIdScheme(), "1_USD"));
    assertNotNull(ycdoc);
    final YieldCurveDefinition yc = ycdoc.getYieldCurveDefinition();
    assertNotNull(yc);
    assertEquals("A", yc.getInterpolatorName());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGet_missing() {
    _master.get(UniqueId.of(_master.getUniqueIdScheme(), "GBP_3"));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGet_deleted() {
    _master.remove(UniqueId.of(_master.getUniqueIdScheme(), "USD_1"));
    _master.get(UniqueId.of(_master.getUniqueIdScheme(), "USD_1"));
  }

  @Test
  public void testRemove() {
    assertNotNull(_master.getDefinition(Currency.USD, "1"));
    _master.remove(UniqueId.of(_master.getUniqueIdScheme(), "1_USD"));
    assertNull(_master.getDefinition(Currency.USD, "1"));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemove_missing() {
    _master.remove(UniqueId.of(_master.getUniqueIdScheme(), "3_USD"));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdate_missing() {
    _master.update(new YieldCurveDefinitionDocument(UniqueId.of(_master.getUniqueIdScheme(), "3_USD"), new YieldCurveDefinition(Currency.USD, null, "3", "E", "L1", "R1", true)));
  }

}
