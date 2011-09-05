/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    ExternalId object = ExternalId.of("A", "B");
    assertEncodeDecodeCycle(ExternalId.class, object);
  }

}
