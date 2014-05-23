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
import com.opengamma.util.time.Tenor;

/**
 * Tests the fields of the ibor index. This test is intended to pick up any changes to the
 * ibor index before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class IborIndexTest {
  /** The index name */
  private static final String NAME = "3M USD LIBOR";
  /** The index description */
  private static final String DESCRIPTION = "LIBOR DESCRIPTION";
  /** The tickers */
  private static final ExternalIdBundle TICKERS = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("US0003M Index"),
      ExternalSchemes.syntheticSecurityId(NAME));
  /** The tenor */
  private static final Tenor TENOR = Tenor.ofMonths(3);
  /** The convention id */
  private static final ExternalId CONVENTION_ID = ExternalId.of("SCHEME", "USD LIBOR CONVENTION");
  /** The index */
  private static final IborIndex INDEX_NO_DESCRIPTION = new IborIndex(NAME, TENOR, CONVENTION_ID);
  /** The index */
  private static final IborIndex INDEX_WITH_DESCRIPTION = new IborIndex(NAME, DESCRIPTION, TENOR, CONVENTION_ID);

  static {
    INDEX_NO_DESCRIPTION.setExternalIdBundle(TICKERS);
    INDEX_WITH_DESCRIPTION.setExternalIdBundle(TICKERS);
  }
  /**
   * Tests that the tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    new IborIndex(NAME, DESCRIPTION, null, CONVENTION_ID);
  }

  /**
   * Tests that the convention id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionId() {
    new IborIndex(NAME, DESCRIPTION, TENOR, null);
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
    assertEquals(TICKERS, INDEX_NO_DESCRIPTION.getExternalIdBundle());
    assertEquals(TICKERS, INDEX_WITH_DESCRIPTION.getExternalIdBundle());
    assertEquals(TENOR, INDEX_NO_DESCRIPTION.getTenor());
    assertEquals(TENOR, INDEX_WITH_DESCRIPTION.getTenor());
    assertEquals(CONVENTION_ID, INDEX_NO_DESCRIPTION.getConventionId());
    assertEquals(CONVENTION_ID, INDEX_WITH_DESCRIPTION.getConventionId());
  }
}
