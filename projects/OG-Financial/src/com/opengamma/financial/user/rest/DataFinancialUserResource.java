/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;

import com.opengamma.financial.user.FinancialUser;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a single user.
 * <p>
 * This resource receives and processes RESTful calls.
 */
public class DataFinancialUserResource extends AbstractDataResource {

  /**
   * The user.
   */
  private final FinancialUser _user;

  /**
   * Creates an instance.
   * 
   * @param user  the user, not null
   */
  public DataFinancialUserResource(FinancialUser user) {
    _user = user;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user.
   * 
   * @return the user, not null
   */
  public FinancialUser getUser() {
    return _user;
  }

  //-------------------------------------------------------------------------
  @Path("clients")
  public DataFinancialClientManagerResource findClients() {
    return new DataFinancialClientManagerResource(getUser().getClientManager());
  }

}
