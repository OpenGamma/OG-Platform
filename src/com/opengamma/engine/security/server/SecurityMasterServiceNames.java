/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

/**
 * A class of names used in the RESTful security master.
 */
public class SecurityMasterServiceNames {

  public static final String DEFAULT_SECURITYMASTER_NAME = "0";

  /**
   * Fudge key for a list of securities.
   */
  public static final String SECURITYMASTER_SECURITIES = "securities";
  /**
   * Fudge key for a security.
   */
  public static final String SECURITYMASTER_SECURITY = "security";
  /**
   * Fudge key for all security types.
   */
  public static final String SECURITYMASTER_ALLSECURITYTYPES = "allSecurityTypes";

  /**
   * No instances.
   */
  private SecurityMasterServiceNames() {
  }

}
