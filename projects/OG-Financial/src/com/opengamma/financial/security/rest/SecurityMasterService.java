/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteSecurityMaster}.
 */
@Path("securityMaster")
public class SecurityMasterService extends AbstractResourceService<SecurityMaster, SecurityMasterResource> {

  public SecurityMasterService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected SecurityMasterResource createResource(SecurityMaster underlying) {
    return new SecurityMasterResource(underlying, getFudgeContext());
  }

}
