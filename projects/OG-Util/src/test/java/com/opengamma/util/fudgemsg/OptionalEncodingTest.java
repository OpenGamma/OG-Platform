/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding of Guava Optionals.
 */
@Test(groups = TestGroup.UNIT)
public class OptionalEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_populated() {
    Optional<String> object = Optional.of("Hello");
    assertEncodeDecodeCycle(Optional.class, object);
  }

  public void test_empty() {
    Optional<String> object = Optional.absent();
    assertEncodeDecodeCycle(Optional.class, object);
  }

  public void test_nested_populated() {
    Optional<Optional<String>> object = Optional.of(Optional.of("Hello"));
    assertEncodeDecodeCycle(Optional.class, object);
  }

  public void test_nested_empty() {
    Optional<Optional<Object>> object = Optional.of(Optional.absent());
    assertEncodeDecodeCycle(Optional.class, object);
  }
}
