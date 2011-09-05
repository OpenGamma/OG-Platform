/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class UniqueIdFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test_simple() {
    UniqueId object = UniqueId.of("A", "B");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

  public void test_versioned() {
    UniqueId object = UniqueId.of("A", "B", "C");
    assertEncodeDecodeCycle(UniqueId.class, object);
  }

}
