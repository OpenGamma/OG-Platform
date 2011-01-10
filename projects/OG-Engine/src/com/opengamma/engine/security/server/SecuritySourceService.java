/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.security.RemoteSecuritySource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteSecuritySource}.
 */
@Path("securitySource")
public class SecuritySourceService extends AbstractResourceService<SecuritySource, SecuritySourceResource> {

  /**
   * Creates an instance using the specified Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public SecuritySourceService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected SecuritySourceResource createResource(SecuritySource underlying) {
    return new SecuritySourceResource(getFudgeContext(), underlying);
  }

}
