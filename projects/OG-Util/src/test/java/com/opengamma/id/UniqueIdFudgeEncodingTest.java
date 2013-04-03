/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_simple() {
    UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_versioned() {
    UniqueId object = UniqueId.of("A", "B", "C");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

}
