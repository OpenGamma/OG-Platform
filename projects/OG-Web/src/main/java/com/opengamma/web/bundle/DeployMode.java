/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

/**
 * Deployment mode, represents Development or Production mode.
 */
public enum DeployMode {

  /**
   * Development mode.
   * Bundles will not be compressed in development.
   */
  DEV,
  /**
   * Production mode.
   * Bundles will be compressed in production.
   */
  PROD

}
