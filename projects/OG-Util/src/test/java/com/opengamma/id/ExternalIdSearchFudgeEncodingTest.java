/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdSearchFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_empty() {
    ExternalIdSearch object = new ExternalIdSearch();
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  public void test_id() {
    ExternalIdSearch object = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  public void test_full() {
    ExternalIdSearch object = new ExternalIdSearch(Arrays.asList(ExternalId.of("A", "B")), ExternalIdSearchType.EXACT);
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

}
