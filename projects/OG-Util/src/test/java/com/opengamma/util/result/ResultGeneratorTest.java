/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ResultGeneratorTest {

  @Test
  public void anyFailures() {
    Result<String> success1 = ResultGenerator.success("success 1");
    Result<String> success2 = ResultGenerator.success("success 1");
    Result<Object> failure1 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "failure 1");
    Result<Object> failure2 = ResultGenerator.failure(FailureStatus.ERROR, "failure 2");
    assertTrue(ResultGenerator.anyFailures(failure1, failure2));
    assertTrue(ResultGenerator.anyFailures(failure1, success1));
    assertFalse(ResultGenerator.anyFailures(success1, success2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void propagateFailures() {
    Result<String> success1 = ResultGenerator.success("success 1");
    Result<String> success2 = ResultGenerator.success("success 1");
    Result<Object> failure1 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "failure 1");
    Result<Object> failure2 = ResultGenerator.failure(FailureStatus.ERROR, "failure 2");
    Result<Object> composite1 = ResultGenerator.propagateFailures(success1,
                                                                  success2,
                                                                  failure1,
                                                                  failure2);
    List<?> failures = ((MultipleFailureResult<?>) composite1).getFailures();
    assertEquals(Lists.newArrayList(failure1, failure2), failures);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void propagateSuccesses() {
    Result<String> success1 = ResultGenerator.success("success 1");
    Result<String> success2 = ResultGenerator.success("success 1");
    ResultGenerator.propagateFailures(success1, success2);
  }
}
