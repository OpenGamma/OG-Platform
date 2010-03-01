package com.opengamma.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;

public class ProbabilisticTestCase {

  /**
   * Runs the calling method up to {@code n} times. Returns {@code true} if the
   * calling method should return immediately,
   * returns {@code false} if the calling method should continue. E.g.
   * 
   * <pre>
   * &#064;Test
   * public void test() {
   *   if (retry(2))
   *     return;
   *   // main test ...
   * }
   * </pre>
   * 
   * @author Andrew Griffin
   */
  protected boolean retry(final int n) {
    // get the call stack
    final Throwable t = new Throwable();
    t.fillInStackTrace();
    final StackTraceElement[] stack = t.getStackTrace();
    // take no action if we've already been called (i.e. another retry is
    // controlling execution)
    if (stack[6].getClassName().equals(getClass().getName()) && stack[6].getMethodName().equals("retry"))
      return false;
    // get the test method to retry
    assertEquals(getClass().getName(), stack[1].getClassName());
    final Method test;
    try {
      test = getClass().getDeclaredMethod(stack[1].getMethodName());
    } catch (final SecurityException e) {
      throw new OpenGammaRuntimeException("test case failed", e);
    } catch (final NoSuchMethodException e) {
      throw new OpenGammaRuntimeException("test case failed", e);
    }
    assertNotNull(test);
    AssertionError testError = null;
    for (int i = 0; i < n; i++) {
      try {
        test.invoke(this);
        // it worked if we got here
        return true;
      } catch (final IllegalArgumentException e) {
        throw new OpenGammaRuntimeException("test case failed", e);
      } catch (final IllegalAccessException e) {
        throw new OpenGammaRuntimeException("test case failed", e);
      } catch (final InvocationTargetException e) {
        if (e.getCause() instanceof AssertionError) {
          testError = (AssertionError) e.getCause();
        } else {
          throw new OpenGammaRuntimeException("test case failed", e);
        }
      }
    }
    // it didn't work if we got here
    assertNotNull(testError);
    throw testError;
  }

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
