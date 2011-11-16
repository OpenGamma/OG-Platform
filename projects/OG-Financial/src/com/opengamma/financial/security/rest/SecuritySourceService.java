/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteFinancialSecuritySource}.
 */
@Path("securitySource")
public class SecuritySourceService extends AbstractResourceService<FinancialSecuritySource, SecuritySourceResource> {

  /**
   * Creates an instance using the specified Fudge context.
   * 
   * @param fudgeContext  the Fudge context, not null
   */
  public SecuritySourceService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected SecuritySourceResource createResource(FinancialSecuritySource underlying) {
    return new SecuritySourceResource(getFudgeContext(), underlying);
  }

}
