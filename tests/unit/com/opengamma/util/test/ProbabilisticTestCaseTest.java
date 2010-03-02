package com.opengamma.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.util.test.ProbabilisticTestCase;

public class ProbabilisticTestCaseTest extends ProbabilisticTestCase {

  private int _testRetry_failFirstCount = 0;

  @Test
  public void testRetry_failFirst() {
    if (retry(3))
      return;
    _testRetry_failFirstCount++;
    assertEquals(0, _testRetry_failFirstCount % 2);
  }

  private int _testRetry_failSecondCount = 0;

  @Test
  public void testRetry_failSecond() {
    if (retry(3))
      return;
    _testRetry_failSecondCount++;
    assertEquals(1, _testRetry_failSecondCount % 2);
  }

  @Test(expected = java.lang.AssertionError.class)
  public void testRetry_alwaysFails() {
    if (retry(3))
      return;
    fail("I always fail");
  }

}
