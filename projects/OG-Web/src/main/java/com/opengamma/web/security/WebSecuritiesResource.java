/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.security.ManageableSecurity;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
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
   * @param htsMaster  the historical time series master, not null
   * @param legalEntityMaster the organization master, not null
   */
  public WebSecuritiesResource(
      final SecurityMaster securityMaster, final SecurityLoader securityLoader, final HistoricalTimeSeriesMaster htsMaster, final LegalEntityMaster legalEntityMaster) {
    super(securityMaster, securityLoader, htsMaster, legalEntityMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.SECURITY)
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
    return getFreemarker().build(HTML_DIR + "securities.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.SECURITY)
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
    return getFreemarker().build(JSON_DIR + "securities.ftl", out);
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
      @FormParam("type") String type,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam(SECURITY_XML) String securityXml) {
    
    type = StringUtils.defaultString(StringUtils.trimToNull(type));
    FlexiBean out = createRootData();
    URI responseURI = null;
    
    switch (type) {
      case "xml":
        try {
          securityXml = StringUtils.trimToNull(securityXml);
          
          ArgumentChecker.notNull(securityXml, "securityXml");
          ManageableSecurity security = addSecurity(securityXml);
          WebSecuritiesUris webSecuritiesUris = new WebSecuritiesUris(data());
          responseURI =  webSecuritiesUris.security(security);
        } catch (Exception ex) {
          out.put("err_securityXml", true);
          out.put("err_securityXmlMsg", ex.getMessage());
          out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(securityXml)));
          String html = getFreemarker().build(HTML_DIR + "securities-add.ftl", out);
          return Response.ok(html).build();
        }
        break;
      case "id":
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        
        if (idScheme == null || idValue == null) {
          if (idScheme == null) {
            out.put("err_idschemeMissing", true);
          }
          if (idValue == null) {
            out.put("err_idvalueMissing", true);
          }
          out.put("idscheme", idScheme);
          out.put("idvalue", idValue);
          String html = getFreemarker().build(HTML_DIR + "securities-add.ftl", out);
          return Response.ok(html).build();
        }
        
        ExternalScheme scheme = ExternalScheme.of(idScheme);
        Collection<ExternalIdBundle> bundles = buildSecurityRequest(scheme, idValue);
        SecurityLoader securityLoader = data().getSecurityLoader();
        Map<ExternalIdBundle, UniqueId> loadedSecurities = securityLoader.loadSecurities(bundles);
        
        if (bundles.size() == 1 && loadedSecurities.size() == 1) {
          ExternalIdBundle identifierBundle = bundles.iterator().next();
          responseURI = data().getUriInfo().getAbsolutePathBuilder().path(loadedSecurities.get(identifierBundle).toLatest().toString()).build();
        } else {
          responseURI = uri(data(), buildRequestAsExternalIdBundle(scheme, bundles));
        }
        break;
      default:
        throw new IllegalArgumentException("Can only add security by XML or ID");
    }    
    return Response.seeOther(responseURI).build();
  }

  private ManageableSecurity addSecurity(String securityXml) {
    Bean securityBean = JodaBeanSerialization.deserializer().xmlReader().read(securityXml);
    SecurityMaster securityMaster = data().getSecurityMaster();
    ManageableSecurity manageableSecurity = (ManageableSecurity) securityBean;
    manageableSecurity.setUniqueId(null);
    SecurityDocument addedSecDoc = securityMaster.add(new SecurityDocument(manageableSecurity));
    return addedSecDoc.getSecurity();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("type") String type,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam(SECURITY_XML) String securityXml) {
    
    FlexiBean out = createRootData();
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    out.put("requestScheme", scheme);
    
    type = StringUtils.defaultString(StringUtils.trimToNull(type), "");
    switch (type) {
      case "xml":
        securityXml = StringUtils.trimToNull(securityXml);
        if (securityXml == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        ManageableSecurity security = addSecurity(securityXml);
        out.put("addedSecurities", getAddedSecurityId(security));
        break;
      case "": // create security by ID if type is missing
      case "id":
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        if (idScheme == null || idValue == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        Collection<ExternalIdBundle> requestBundles = buildSecurityRequest(scheme, idValue);
        SecurityLoader securityLoader = data().getSecurityLoader();
        out.put("addedSecurities", getLoadedSecuritiesId(securityLoader.loadSecurities(requestBundles), requestBundles, scheme));
        break;
      default:
        throw new IllegalArgumentException("Can only add security by XML or ID");
    }    
    return Response.ok(getFreemarker().build(JSON_DIR + "securities-added.ftl", out)).build();
  }

  private Map<String, String> getAddedSecurityId(ManageableSecurity security) {
    Map<String, String> addedSecurities = new HashMap<>();
    ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    UniqueId uniqueId = security.getUniqueId();
    String objectIdentifier = uniqueId.getObjectId().toString();
    String externalIdValue = "";
    if (!externalIdBundle.isEmpty()) {
      ExternalId externalId = externalIdBundle.iterator().next();
      externalIdValue = externalId.getValue();
    } 
    addedSecurities.put(externalIdValue, objectIdentifier);
    return addedSecurities;
  }

  private Map<String, String> getLoadedSecuritiesId(Map<ExternalIdBundle, UniqueId> loadedSecurities, Collection<ExternalIdBundle> requestBundles, ExternalScheme scheme) {
    Map<String, String> result = new HashMap<String, String>();
    for (ExternalIdBundle identifierBundle : requestBundles) {
      UniqueId uniqueIdentifier = loadedSecurities.get(identifierBundle);
      String objectIdentifier = uniqueIdentifier != null ? uniqueIdentifier.getObjectId().toString() : null;
      result.put(identifierBundle.getValue(scheme), objectIdentifier);
    }
    return result;
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
    return getFreemarker().build(JSON_DIR + "metadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{securityId}")
  public WebSecurityResource findSecurity(@Subscribe @PathParam("securityId") String idStr) {
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
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setSchemaVersion(true);
    SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(request);
    out.put("securityTypes", new TreeSet<String>(metaData.getSecurityTypes()));
    out.put("schemaVersion", metaData.getSchemaVersion());
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
