/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BarrierTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKnock() {
    new Barrier(null, BarrierType.DOWN, ObservationType.CONTINUOUS, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBarrierType() {
    new Barrier(KnockType.IN, null, ObservationType.CONTINUOUS, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObservationType() {
    new Barrier(KnockType.IN, BarrierType.DOWN, null, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, -100);
  }

  @Test
  public void test() {
    final Barrier barrier = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 100);
    assertEquals(barrier.getBarrierType(), BarrierType.DOWN);
    assertEquals(barrier.getBarrierLevel(), 100, 0);
    assertEquals(barrier.getKnockType(), KnockType.IN);
    assertEquals(barrier.getObservationType(), ObservationType.CONTINUOUS);
    Barrier other = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 100);
    assertEquals(barrier, other);
    assertEquals(barrier.hashCode(), other.hashCode());
    other = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 100);
    assertFalse(barrier.equals(other));
    other = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 100);
    assertFalse(barrier.equals(other));
    other = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 100);
    assertFalse(barrier.equals(other));
    other = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 110);
    assertFalse(barrier.equals(other));
  }
}
