/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 *
 */
public class FutureDefinitionTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
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
