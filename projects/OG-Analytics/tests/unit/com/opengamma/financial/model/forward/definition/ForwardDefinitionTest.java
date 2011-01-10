/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 *
 */
public class ForwardDefinitionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new ForwardDefinition(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final ForwardDefinition def1 = new ForwardDefinition(new Expiry(DateUtil.getUTCDate(2010, 1, 1)));
    final ForwardDefinition def2 = new ForwardDefinition(new Expiry(DateUtil.getUTCDate(2010, 1, 1)));
    final ForwardDefinition def3 = new ForwardDefinition(new Expiry(DateUtil.getUTCDate(2010, 2, 1)));
    assertEquals(def1, def2);
    assertEquals(def1.hashCode(), def2.hashCode());
    assertFalse(def2.equals(def3));
  }
}
