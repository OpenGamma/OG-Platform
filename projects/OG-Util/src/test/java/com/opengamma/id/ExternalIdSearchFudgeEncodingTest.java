/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdSearchFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_empty() {
    ExternalIdSearch object = ExternalIdSearch.of();
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  public void test_id() {
    ExternalIdSearch object = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  public void test_full() {
    ExternalIdSearch object = ExternalIdSearch.of(ExternalIdSearchType.EXACT, Arrays.asList(ExternalId.of("A", "B")));
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

}
