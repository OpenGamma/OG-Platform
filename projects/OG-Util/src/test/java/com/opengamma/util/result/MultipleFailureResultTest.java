/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MultipleFailureResultTest {

  @Test
  public void sameType() {
    Result<Object> failure1 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "message 1");
    Result<Object> failure2 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "message 2");
    Result<Object> failure3 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "message 3");
    List<Result<?>> failures = Lists.<Result<?>>newArrayList(failure1, failure2, failure3);
    MultipleFailureResult<Object> composite = new MultipleFailureResult<>(failures);
    AssertJUnit.assertEquals(FailureStatus.MISSING_DATA, composite.getStatus());
    AssertJUnit.assertEquals("message 1\nmessage 2\nmessage 3", composite.getFailureMessage());
  }

  @Test
  public void differentTypes() {
    Result<Object> failure1 = ResultGenerator.failure(FailureStatus.MISSING_DATA, "message 1");
    Result<Object> failure2 = ResultGenerator.failure(FailureStatus.CALCULATION_FAILED, "message 2");
    Result<Object> failure3 = ResultGenerator.failure(FailureStatus.ERROR, "message 3");
    List<Result<?>> failures = Lists.<Result<?>>newArrayList(failure1, failure2, failure3);
    MultipleFailureResult<Object> composite = new MultipleFailureResult<>(failures);
    AssertJUnit.assertEquals(FailureStatus.MULTIPLE, composite.getStatus());
    AssertJUnit.assertEquals("message 1\nmessage 2\nmessage 3", composite.getFailureMessage());
  }
}
