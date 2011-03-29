/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;

/**
 * 
 */
public class BarrierTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKnock() {
    new Barrier(null, BarrierType.DOWN, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBarrierType() {
    new Barrier(KnockType.IN, null, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new Barrier(KnockType.IN, BarrierType.DOWN, -100);
  }

  @Test
  public void test() {
    final Barrier barrier = new Barrier(KnockType.IN, BarrierType.DOWN, 100);
    assertEquals(barrier.getBarrierType(), BarrierType.DOWN);
    assertEquals(barrier.getBarrierLevel(), 100, 0);
    assertEquals(barrier.getKnockType(), KnockType.IN);
    Barrier other = new Barrier(KnockType.IN, BarrierType.DOWN, 100);
    assertEquals(barrier, other);
    assertEquals(barrier.hashCode(), other.hashCode());
    other = new Barrier(KnockType.OUT, BarrierType.DOWN, 100);
    assertFalse(barrier.equals(other));
    other = new Barrier(KnockType.IN, BarrierType.UP, 100);
    assertFalse(barrier.equals(other));
    other = new Barrier(KnockType.IN, BarrierType.DOWN, 110);
    assertFalse(barrier.equals(other));
  }
}
