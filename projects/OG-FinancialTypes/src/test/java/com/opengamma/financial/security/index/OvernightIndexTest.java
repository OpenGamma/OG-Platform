/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.lang.reflect.Field;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the fields of the overnight index. This test is intended to pick up any changes to the
 * overnight index before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class OvernightIndexTest {
  /** The index name */
  private static final String NAME = "USD OVERNIGHT";
  /** The index description */
  private static final String DESCRIPTION = "OVERNIGHT DESCRIPTION";
  /** The tickers */
  private static final ExternalIdBundle TICKERS = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("US0003M Index"),
      ExternalSchemes.syntheticSecurityId(NAME));
  /** The convention id */
  private static final ExternalId CONVENTION_ID = ExternalId.of("SCHEME", "USD OVERNIGHT CONVENTION");
  /** The index */
  private static final OvernightIndex INDEX_NO_DESCRIPTION = new OvernightIndex(NAME, CONVENTION_ID);
  /** The index */
  private static final OvernightIndex INDEX_WITH_DESCRIPTION = new OvernightIndex(NAME, DESCRIPTION, CONVENTION_ID);

  static {
    INDEX_NO_DESCRIPTION.setExternalIdBundle(TICKERS);
    INDEX_WITH_DESCRIPTION.setExternalIdBundle(TICKERS);
  }
  /**
   * Tests that the convention id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionId() {
    new OvernightIndex(NAME, DESCRIPTION,  null);
  }

  /**
   * Tests the number of fields in the index. Will pick up additions / removals.
   */
  @Test
  public void testNumberOfFields() {
    List<Field> fields = IndexTestUtils.getFields(INDEX_NO_DESCRIPTION.getClass());
    assertEquals(14, fields.size());
    fields = IndexTestUtils.getFields(INDEX_WITH_DESCRIPTION.getClass());
    assertEquals(14, fields.size());
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
    assertEquals(TICKERS, INDEX_NO_DESCRIPTION.getExternalIdBundle());
    assertEquals(TICKERS, INDEX_WITH_DESCRIPTION.getExternalIdBundle());
    assertEquals(CONVENTION_ID, INDEX_NO_DESCRIPTION.getConventionId());
    assertEquals(CONVENTION_ID, INDEX_WITH_DESCRIPTION.getConventionId());
  }
}
