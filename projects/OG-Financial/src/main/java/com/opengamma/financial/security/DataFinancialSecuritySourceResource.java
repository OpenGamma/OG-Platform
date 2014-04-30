/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.DataSecuritySourceResource;
import com.opengamma.util.fudgemsg.FudgeListWrapper;

/**
 * RESTful resource for securities.
 * <p>
 * The securities resource receives and processes RESTful calls to the security source.
 */
public class DataFinancialSecuritySourceResource extends DataSecuritySourceResource {

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param securitySource  the underlying security source, not null
   */
  public DataFinancialSecuritySourceResource(final SecuritySource securitySource) {
    super(securitySource);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security source.
   * 
   * @return the security source, not null
   */
  public FinancialSecuritySource getSecuritySource() {
    return (FinancialSecuritySource) super.getSecuritySource();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("securities/bonds")
  public Response searchBonds(@QueryParam("issuerName") String issuerName) {
    FinancialSecuritySource source = (FinancialSecuritySource) getSecuritySource();
    Collection<? extends Security> result = source.getBondsWithIssuerName(issuerName);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param issuerName  the issuer name, may be null
   * @return the URI, not null
   */
  public static URI uriSearchBonds(URI baseUri, String issuerName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/securities/bonds");
    bld.queryParam("issuerName", issuerName);
    return bld.build();
  }

}
