/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the fields of a bond index component. This test is intended to pick up any changes
 * before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class BondIndexComponentTest {
  /** The bond ids */
  private static final ExternalIdBundle IDS = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("AAA"),
      ExternalSchemes.bloombergBuidSecurityId("AAAA"));
  /** The weight */
  private static final BigDecimal WEIGHT = new BigDecimal(1234);
  /** The component */
  private static final BondIndexComponent COMPONENT = new BondIndexComponent(IDS, WEIGHT);

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIds() {
    new BondIndexComponent(null, WEIGHT);
  }

  /**
   * Tests that the weight cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeight() {
    new BondIndexComponent(IDS, null);
  }

  /**
   * Tests the number of fields in the index component.
   */
  @Test
  public void testNumberOfFields() {
    assertEquals(3, IndexTestUtils.getFields(COMPONENT.getClass()).size());
  }

  /**
   * Tests that the fields are set correctly.
   */
  @Test
  public void testFields() {
    assertEquals(IDS, COMPONENT.getBondIdentifier());
    assertEquals(WEIGHT, COMPONENT.getWeight());
  }
}
