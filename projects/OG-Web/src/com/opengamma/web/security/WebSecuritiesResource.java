/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for all securities.
 * <p>
 * The securities resource represents the whole of a security master.
 */
@Path("/securities")
public class WebSecuritiesResource extends AbstractWebSecurityResource {
  private static final Logger s_logger = LoggerFactory.getLogger(WebSecuritiesResource.class);
  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   */
  public WebSecuritiesResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader) {
    super(securityMaster, securityLoader);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setSecurityType(StringUtils.trimToNull(type));
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addIdentifierBundle(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      SecuritySearchResult searchResult = data().getSecurityMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return getFreemarker().build("securities/securities.ftl", out);
  }

//-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    FlexiBean out = createRootData();
    if (idScheme == null || idValue == null) {
      if (idScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      }
      String html = getFreemarker().build("securities/securities-add.ftl", out);
      return Response.ok(html).build();
    }
    List<IdentifierBundle> errors = new ArrayList<IdentifierBundle>();
    IdentificationScheme scheme = IdentificationScheme.of(idScheme);
    Collection<IdentifierBundle> bundles = buildSecurityRequest(scheme, idValue);
    SecurityLoader securityLoader = data().getSecurityLoader();
    Map<IdentifierBundle, ManageableSecurity> loadedSecurities = securityLoader.loadSecurity(bundles);
    SecurityMaster securityMaster = data().getSecurityMaster();
    SecurityDocument added = null;
    for (IdentifierBundle identifierBundle : bundles) {
      ManageableSecurity security = loadedSecurities.get(identifierBundle);
      if (security != null) {
        try {
          final SecurityDocument document = new SecurityDocument();
          document.setSecurity(security);
          added = securityMaster.add(document);
        } catch (Exception e) {
          s_logger.warn("error writing security=" + security, e.getCause());
          errors.add(identifierBundle);
        }
      } else {
        errors.add(identifierBundle);
      }
    }
    URI uri = null;
    if (bundles.size() == 1) {
      uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getSecurityId().toLatest().toString()).build();
    } else {
      uri = uri(data(), buildRequestAsIdentifierBundle(scheme, bundles));
//      uri = uri(data());
    }
    return Response.seeOther(uri).build();
  }

  private IdentifierBundle buildRequestAsIdentifierBundle(IdentificationScheme scheme, Collection<IdentifierBundle> bundles) {
    List<Identifier> identifiers = new ArrayList<Identifier>();
    for (IdentifierBundle bundle : bundles) {
      String identifierValue = bundle.getIdentifier(scheme);
      identifiers.add(Identifier.of(scheme, identifierValue));
    }
    return IdentifierBundle.of(identifiers);
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
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebSecuritiesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebSecuritiesData data, IdentifierBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebSecuritiesResource.class);
    if (identifiers != null) {
      Iterator<Identifier> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        Identifier id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }
  
  private Collection<IdentifierBundle> buildSecurityRequest(final IdentificationScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptyList();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final List<IdentifierBundle> result = new ArrayList<IdentifierBundle>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(IdentifierBundle.of(Identifier.of(identificationScheme, identifier)));
      }
    }
    return result;
  }

}
