/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

/**
 * A class of names used in the RESTful security master.
 */
public final class SecurityMasterServiceNames {

  /*package*/ static final String DEFAULT_SECURITYMASTER_NAME = "0";

  /**
   * Fudge key for a list of securities.
   */
  public static final String SECURITYMASTER_SECURITIES = "securities";
  /**
   * Fudge key for a security.
   */
  public static final String SECURITYMASTER_SECURITY = "security";

  /**
   * No instances.
   */
  private SecurityMasterServiceNames() {
  }

}
