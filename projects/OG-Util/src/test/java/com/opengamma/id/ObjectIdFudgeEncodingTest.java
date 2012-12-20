/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ObjectIdFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    ObjectId object = ObjectId.of("A", "B");
    assertEncodeDecodeCycle(ObjectId.class, object);
  }

}
