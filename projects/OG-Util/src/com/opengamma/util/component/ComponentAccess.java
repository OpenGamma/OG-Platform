/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

/**
 * Different ways to access the component.
 */
public enum ComponentAccess {

  /**
   * Access is provided over REST, typically using Fudge.
   */
  REST,
  /**
   * Access is provided by a raw socket.
   */
  SOCKET,

}
