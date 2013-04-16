/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test LiveDataSpecification.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataSpecificationTest extends AbstractFudgeBuilderTestCase {

  public void test_basic() {
    LiveDataSpecification object = new LiveDataSpecification("Foo", ExternalId.of("bar", "baz"));
    assertEncodeDecodeCycle(LiveDataSpecification.class, object);
  }

}
