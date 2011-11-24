/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.sun.jersey.api.client.ClientResponse.Status;

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
   * @param securityLoader  the security loader, not null
   */
  public WebSecuritiesResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader) {
    super(securityMaster, securityLoader);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("identifier") String identifier,
      @QueryParam("type") String type,
      @QueryParam("securityId") List<String> securityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, identifier, type, securityIdStrs, uriInfo);
    return getFreemarker().build("securities/securities.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("identifier") String identifier,
      @QueryParam("type") String type,
      @QueryParam("securityId") List<String> securityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, identifier, type, securityIdStrs, uriInfo);
    return getFreemarker().build("securities/jsonsecurities.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, SecuritySearchSortOrder so, String name, String identifier,
      String type, List<String> securityIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setExternalIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setSecurityType(StringUtils.trimToNull(type));
    for (String securityIdStr : securityIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(securityIdStr));
    }
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      SecuritySearchResult searchResult = data().getSecurityMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
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
      out.put("idscheme", idScheme);
      out.put("idvalue", idValue);
      String html = getFreemarker().build("securities/securities-add.ftl", out);
      return Response.ok(html).build();
    }
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    Collection<ExternalIdBundle> bundles = buildSecurityRequest(scheme, idValue);
    SecurityLoader securityLoader = data().getSecurityLoader();
    Map<ExternalIdBundle, UniqueId> loadedSecurities = securityLoader.loadSecurity(bundles);
    
    URI uri = null;
    if (bundles.size() == 1 && loadedSecurities.size() == 1) {
      ExternalIdBundle identifierBundle = bundles.iterator().next();
      uri = data().getUriInfo().getAbsolutePathBuilder().path(loadedSecurities.get(identifierBundle).toLatest().toString()).build();
    } else {
      uri = uri(data(), buildRequestAsExternalIdBundle(scheme, bundles));
    }
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    if (idScheme == null || idValue == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    Collection<ExternalIdBundle> requestBundles = buildSecurityRequest(scheme, idValue);
    SecurityLoader securityLoader = data().getSecurityLoader();
    Map<ExternalIdBundle, UniqueId> loadedSecurities = securityLoader.loadSecurity(requestBundles);
    FlexiBean out = createPostJSONOutput(loadedSecurities, requestBundles, scheme);    
    return Response.ok(getFreemarker().build("securities/jsonsecurities-added.ftl", out)).build();
  }

  private FlexiBean createPostJSONOutput(Map<ExternalIdBundle, UniqueId> loadedSecurities, Collection<ExternalIdBundle> requestBundles, ExternalScheme scheme) {
    Map<String, String> result = new HashMap<String, String>();
    for (ExternalIdBundle identifierBundle : requestBundles) {
      UniqueId uniqueIdentifier = loadedSecurities.get(identifierBundle);
      String objectIdentifier = uniqueIdentifier != null ? uniqueIdentifier.getObjectId().toString() : null;
      result.put(identifierBundle.getValue(scheme), objectIdentifier);
    }
    FlexiBean out = createRootData();
    out.put("requestScheme", scheme);
    out.put("addedSecurities", result);
    return out;
  }

  private ExternalIdBundle buildRequestAsExternalIdBundle(ExternalScheme scheme, Collection<ExternalIdBundle> bundles) {
    List<ExternalId> identifiers = new ArrayList<ExternalId>();
    for (ExternalIdBundle bundle : bundles) {
      identifiers.add(bundle.getExternalId(scheme));
    }
    return ExternalIdBundle.of(identifiers);
  }

  private Collection<ExternalIdBundle> buildSecurityRequest(final ExternalScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptyList();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final List<ExternalIdBundle> result = new ArrayList<ExternalIdBundle>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(ExternalIdBundle.of(ExternalId.of(identificationScheme, identifier)));
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/jsonmetadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{securityId}")
  public WebSecurityResource findSecurity(@PathParam("securityId") String idStr) {
    data().setUriSecurityId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      SecurityDocument doc = data().getSecurityMaster().get(oid);
      data().setSecurity(doc);
    } catch (DataNotFoundException ex) {
      SecurityHistoryRequest historyRequest = new SecurityHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      SecurityHistoryResult historyResult = data().getSecurityMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setSecurity(historyResult.getFirstDocument());
    }
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
    SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(new SecurityMetaDataRequest());
    out.put("securityTypes", new TreeSet<String>(metaData.getSecurityTypes()));
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
  public static URI uri(WebSecuritiesData data, ExternalIdBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebSecuritiesResource.class);
    if (identifiers != null) {
      Iterator<ExternalId> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        ExternalId id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
