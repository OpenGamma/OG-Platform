/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ResultTest {

  @Test
  public void anyFailures() {
    Result<String> success1 = Result.success("success 1");
    Result<String> success2 = Result.success("success 1");
    Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    assertTrue(Result.anyFailures(failure1, failure2));
    assertTrue(Result.anyFailures(failure1, success1));
    assertFalse(Result.anyFailures(success1, success2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void propagateFailures() {
    Result<String> success1 = Result.success("success 1");
    Result<String> success2 = Result.success("success 1");
    Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    Result<Object> composite1 = Result.failure(success1, success2, failure1, failure2);
    List<Failure> failures = composite1.getFailures();
    List<Failure> expected = new ArrayList<>();
    expected.addAll(failure1.getFailures());
    expected.addAll(failure2.getFailures());
    assertEquals(expected, failures);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void propagateSuccesses() {
    Result<String> success1 = Result.success("success 1");
    Result<String> success2 = Result.success("success 1");
    Result.failure(success1, success2);
  }
}
