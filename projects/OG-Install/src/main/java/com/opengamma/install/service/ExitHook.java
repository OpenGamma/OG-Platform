/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.install.service;

/**
 * Exit hook for interacting with a service wrapper.
 */
public final class ExitHook implements Runnable {

  private ExitHook() {
  }

  @Override
  public native void run();

  public static void register() {
    Runtime.getRuntime().addShutdownHook(new Thread(new ExitHook()));
  }

}
