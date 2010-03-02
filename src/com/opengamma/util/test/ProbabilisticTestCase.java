package com.opengamma.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
   * Use this for tests that sometimes fail on the automated builds because of non-deterministic
   * properties or probabilistic behaviors to reduce false positives.
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
    if (stack.length > 50) {
      t.printStackTrace();
      fail("reflection/recursion error");
    }
    for (int i = 1; i < 10; i++) {
      if (stack[i].getClassName().equals(getClass().getName()) && stack[6].getMethodName().equals("retry"))
        return false;
    }
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
      if (i > 0)
        System.out.println("Repeating call to " + test + " (" + i + " of " + (n - 1) + ")");
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

}
