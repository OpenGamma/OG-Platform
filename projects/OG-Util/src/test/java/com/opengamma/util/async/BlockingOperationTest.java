/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link BlockingOperation} class.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BlockingOperationTest {

  public void testDefaultState() {
    assertTrue(BlockingOperation.isOn());
    assertFalse(BlockingOperation.isOff());
  }

  public void testSingleCall() {
    BlockingOperation.off();
    try {
      assertFalse(BlockingOperation.isOn());
      assertTrue(BlockingOperation.isOff());
    } finally {
      BlockingOperation.on();
    }
  }

  public void testNestedCall() {
    BlockingOperation.off();
    try {
      assertFalse(BlockingOperation.isOn());
      assertTrue(BlockingOperation.isOff());
      testSingleCall();
      assertFalse(BlockingOperation.isOn());
      assertTrue(BlockingOperation.isOff());
    } finally {
      BlockingOperation.on();
    }
  }

  private static void operation(final boolean willBlock) {
    if (willBlock) {
      BlockingOperation.wouldBlock();
    }
  }

  public void testBlockingCallWithBlockingOn() {
    operation(true);
  }

  public void testBlockingCallWithBlockingOff() {
    BlockingOperation.off();
    try {
      operation(true);
      fail();
    } catch (BlockingOperation e) {
      assertNotNull(e);
    } finally {
      BlockingOperation.on();
    }
  }

  public void testNonBlockingCallWithBlockingOn() {
    operation(false);
  }

  public void testNonBlockingCallWithBlockingOff() {
    BlockingOperation.off();
    try {
      operation(false);
    } catch (BlockingOperation e) {
      fail();
    } finally {
      BlockingOperation.on();
    }
  }

}
