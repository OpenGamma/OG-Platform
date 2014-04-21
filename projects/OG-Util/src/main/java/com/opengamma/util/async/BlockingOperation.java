/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

/**
 * Utility allowing operations to be implemented in blocking forms by default with a non-blocking mode. If blocking operations (the default) are off then a method may throw this exception in order to
 * indicate a non-blocking failure. The stack frame that most recently disabled blocking exceptions on that thread may then handle the exception.
 * <p>
 * It is only normally wise to disable blocking operations for tasks that are read-only as an otherwise atomic write task could leave the system in an inconsistent state if the blocking exception is
 * thrown by one of its component tasks.
 */
public final class BlockingOperation extends Error {

  private static final long serialVersionUID = 1L;

  private static final class TLS {

    private int _offCount;

  }

  private static final ThreadLocal<TLS> s_tls = new ThreadLocal<TLS>() {
    @Override
    protected TLS initialValue() {
      return new TLS();
    }
  };

  private BlockingOperation() {
  }

  /**
   * Disable blocking operations for the calling thread. This must be matched by a later call to {@link #on}. If blocking operations are already disabled they remain disabled and an internal counter
   * will ensure they will stay off after the corresponding call to {@code on}.
   */
  public static void off() {
    assert s_tls.get()._offCount >= 0;
    s_tls.get()._offCount++;
  }

  /**
   * Restore blocking operations for the calling thread. This must be matched by an earlier call to {@link #off}. If blocking operations were already disabled at the preceding call to {@code off} they
   * will remain off.
   */
  public static void on() {
    assert s_tls.get()._offCount > 0;
    s_tls.get()._offCount--;
  }

  /**
   * If blocking operations are disabled this will throw a {@link BlockingOperation} exception instance. Otherwise this is a no-op.
   */
  public static void wouldBlock() {
    if (isOff()) {
      throw block();
    }
  }

  /**
   * Tests if blocking operations (the default) are enabled.
   * 
   * @return true if operations must block, false if they may throw an exception instead.
   */
  public static boolean isOn() {
    return s_tls.get()._offCount == 0;
  }

  /**
   * Tests if blocking operations are disabled.
   * 
   * @return true if operations may throw an exception, false if they must block
   */
  public static boolean isOff() {
    return s_tls.get()._offCount != 0;
  }

  public static BlockingOperation block() {
    assert isOff();
    return new BlockingOperation();
  }

}
