/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

/**
 * Message delivery directives - must match the codes in Connector/MessageDirectives.h
 */
/* package */final class MessageDirectives {

  /**
   * Prevent construction.
   */
  private MessageDirectives() {
  }

  /**
   * Message will be processed within the "Client" component. These messages are used for housekeeping
   * of the JVM hosting the framework.
   */
  public static final int CLIENT = 1;

  /**
   * Message will be processed by the "User messaging" component. These messages implement the main
   * language integration API and will be handled in a manner appropriate to the language bound to.
   */
  public static final int USER = 2;

}
