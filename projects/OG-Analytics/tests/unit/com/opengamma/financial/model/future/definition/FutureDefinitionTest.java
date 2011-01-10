/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 *
 */
public class FutureDefinitionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new FutureDefinition(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final FutureDefinition def1 = new FutureDefinition(new Expiry(DateUtil.getUTCDate(2010, 1, 1)));
    final FutureDefinition def2 = new FutureDefinition(new Expiry(DateUtil.getUTCDate(2010, 1, 1)));
    final FutureDefinition def3 = new FutureDefinition(new Expiry(DateUtil.getUTCDate(2010, 2, 1)));
    assertEquals(def1, def2);
    assertEquals(def1.hashCode(), def2.hashCode());
    assertFalse(def2.equals(def3));
  }
}
