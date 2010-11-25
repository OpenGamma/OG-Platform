package com.opengamma.financial.security.rest;

import com.opengamma.util.rest.AbstractResourceService;

/**
 * 
 */
public final class SecurityMasterServiceNames {
  
  /**
   * 
   */
  /* package */static final String DEFAULT_SECURITYMASTER_NAME = AbstractResourceService.DEFAULT_RESOURCE_NAME;

  /**
   * For add (POST), get (GET), update/correct (PUT) and remove (DELETE).
   */
  public static final String SECURITYMASTER_SECURITY = "security";
  /**
   * For search (GET).
   */
  public static final String SECURITYMASTER_SEARCH = "search";
  /**
   * For history (GET).
   */
  public static final String SECURITYMASTER_HISTORIC = "historic";

  private SecurityMasterServiceNames() {
  }

}
