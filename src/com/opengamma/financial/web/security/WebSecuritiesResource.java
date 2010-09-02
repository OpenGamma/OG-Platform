/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.security;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.financial.security.master.SecuritySearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * RESTful resource for all securities.
 * <p>
 * The securities resource represents the whole of a security master.
 */
@Path("/securities")
public class WebSecuritiesResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   */
  public WebSecuritiesResource(final SecurityMaster securityMaster) {
    super(securityMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("type") String type) {
    FlexiBean out = createRootData();
    
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setSecurityType(StringUtils.trimToNull(type));
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      SecuritySearchResult searchResult = data().getSecurityMaster().search(searchRequest);
      out.put("searchResult", searchResult);
    }
    return getFreemarker().build("securities/securities.ftl", out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("name") String name,
      @FormParam("type") String type,
      @FormParam("scheme") String scheme,
      @FormParam("schemevalue") String schemeValue) {
    name = StringUtils.trimToNull(name);
    type = StringUtils.trimToNull(type);
    scheme = StringUtils.trimToNull(scheme);
    schemeValue = StringUtils.trimToNull(schemeValue);
    if (name == null || type == null || scheme == null || schemeValue == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (type == null) {
        out.put("err_typeMissing", true);
      }
      if (scheme == null) {
        out.put("err_schemeMissing", true);
      }
      if (schemeValue == null) {
        out.put("err_schemevalueMissing", true);
      }
      String html = getFreemarker().build("securities/securities-add.ftl", out);
      return Response.ok(html).build();
    }
    DefaultSecurity security = new DefaultSecurity(type);
    security.setName(name);
    security.addIdentifier(Identifier.of(scheme, schemeValue));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    SecurityDocument added = data().getSecurityMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getSecurityId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{securityId}")
  public WebSecurityResource findPortfolio(@PathParam("securityId") String idStr) {
    data().setUriSecurityId(idStr);
    SecurityDocument securityDoc = data().getSecurityMaster().get(UniqueIdentifier.parse(idStr));
    data().setSecurity(securityDoc);
    return new WebSecurityResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  public FlexiBean createRootData() {
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    FlexiBean out = getFreemarker().createRootData();
    out.put("searchRequest", searchRequest);
    out.put("uris", new WebSecuritiesUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebSecuritiesData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebSecuritiesResource.class).build();
  }

}
