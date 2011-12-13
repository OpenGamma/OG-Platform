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
   * Local access to the component.
   */
  LOCAL,
  /**
   * RESTful client providing access to a remote instance of the component.
   */
  REST_CLIENT,
  /**
   * RESTful server providing remote access to the local instance of the component.
   */
  REST_SERVER,

}
