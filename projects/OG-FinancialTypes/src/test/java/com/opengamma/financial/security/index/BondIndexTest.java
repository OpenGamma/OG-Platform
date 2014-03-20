/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the fields of a bond index. This test is intended to pick up any changes before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class BondIndexTest {
  /** The index name */
  private static final String NAME = "BOND INDEX";
  /** The index description */
  private static final String DESCRIPTION = "BOND INDEX DESCRIPTION";
  /** The first index member */
  private static final ExternalIdBundle ID1 = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("A"));
  /** The second index member */
  private static final ExternalIdBundle ID2 = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("B"));
  /** The components */
  private static final List<BondIndexComponent> COMPONENTS = Arrays.asList(new BondIndexComponent(ID1, new BigDecimal(0.7)),
      new BondIndexComponent(ID2, new BigDecimal(0.3)));
  /** The weighting type */
  private static final IndexWeightingType WEIGHTING_TYPE = IndexWeightingType.EQUAL;
  /** The index */
  private static final BondIndex INDEX_NO_DESCRIPTION = new BondIndex(NAME, COMPONENTS, WEIGHTING_TYPE);
  /** The index */
  private static final BondIndex INDEX_WITH_DESCRIPTION = new BondIndex(NAME, DESCRIPTION, COMPONENTS, WEIGHTING_TYPE);

  /**
   * Tests that the components cannot be null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComponents() {
    new BondIndex(NAME, DESCRIPTION, null, WEIGHTING_TYPE);
  }

  /**
   * Tests that the components cannot be empty
   */
  @Test
  public void testEmptyComponents() {
    new BondIndex(NAME, DESCRIPTION, new ArrayList<BondIndexComponent>(), WEIGHTING_TYPE);
  }

  /**
   * Tests that the weighting type cannot be null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeightingType() {
    new BondIndex(NAME, DESCRIPTION, COMPONENTS, null);
  }

  /**
   * Tests the number of fields in the index. Will pick up additions / removals.
   */
  @Test
  public void testNumberOfFields() {
    List<Field> fields = IndexTestUtils.getFields(INDEX_NO_DESCRIPTION.getClass());
    assertEquals(15, fields.size());
    fields = IndexTestUtils.getFields(INDEX_WITH_DESCRIPTION.getClass());
    assertEquals(15, fields.size());
  }

  /**
   * Tests that fields are set correctly and that fields that should be null are.
   */
  @Test
  public void test() {
    assertEquals(NAME, INDEX_NO_DESCRIPTION.getName());
    assertEquals(NAME, INDEX_WITH_DESCRIPTION.getName());
    assertNull(INDEX_NO_DESCRIPTION.getDescription());
    assertEquals(DESCRIPTION, INDEX_WITH_DESCRIPTION.getDescription());
    assertEquals(COMPONENTS, INDEX_NO_DESCRIPTION.getBondComponents());
    assertEquals(COMPONENTS, INDEX_WITH_DESCRIPTION.getBondComponents());
    assertEquals(WEIGHTING_TYPE, INDEX_NO_DESCRIPTION.getWeightingType());
    assertEquals(WEIGHTING_TYPE, INDEX_WITH_DESCRIPTION.getWeightingType());
  }
}
