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
import java.util.Map.Entry;

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
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.master.security.impl.DelegatingSecurityMaster;
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
      @QueryParam("uniqueIdScheme") String uniqueIdScheme,
      @QueryParam("securityId") List<String> securityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, identifier, type, uniqueIdScheme, securityIdStrs, uriInfo);
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
      @QueryParam("uniqueIdScheme") String uniqueIdScheme,
      @QueryParam("securityId") List<String> securityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, identifier, type, uniqueIdScheme, securityIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "securities.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, SecuritySearchSortOrder so, String name, String identifier,
      String type, String uniqueIdScheme, List<String> securityIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setExternalIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setSecurityType(StringUtils.trimToNull(type));
    searchRequest.setUniqueIdScheme(StringUtils.trimToNull(uniqueIdScheme));
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
      @FormParam(SECURITY_XML) String securityXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {
    
    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    type = StringUtils.defaultString(StringUtils.trimToNull(type));
    FlexiBean out = createRootData();
    URI responseURI = null;
    switch (type) {
      case "xml":
        boolean isValidInput = true;
        try {
          securityXml = StringUtils.trimToNull(securityXml);
          if (securityXml == null) {
            out.put("err_securityXmlMissing", true);
            isValidInput = false;
          }
          if (uniqueIdScheme == null) {
            out.put("err_unqiueIdSchemeMissing", true);
            isValidInput = false;
          }
          if (!isValidInput) {
            out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(securityXml)));
            out.put("selectedUniqueIdScheme", StringUtils.defaultString(uniqueIdScheme));
            return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
          }
          ManageableSecurity security = addSecurity(securityXml, uniqueIdScheme);
          WebSecuritiesUris webSecuritiesUris = new WebSecuritiesUris(data());
          responseURI =  webSecuritiesUris.security(security);
        } catch (Exception ex) {
          out.put("err_securityXmlMsg", ex.getMessage());
          out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(securityXml)));
          out.put("selectedUniqueIdScheme", StringUtils.defaultString(uniqueIdScheme));
          return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
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
          return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
        }
        
        ExternalScheme scheme = ExternalScheme.of(idScheme);
        Collection<ExternalIdBundle> bundles = buildSecurityRequest(scheme, idValue);
        SecurityLoaderResult loaderResult = data().getSecurityLoader().loadSecurities(SecurityLoaderRequest.create(bundles));
        Map<ExternalIdBundle, UniqueId> loadedSecurities = loaderResult.getResultMap();
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

  private String buildResponseHtml(FlexiBean out, String templateName) {
    return getFreemarker().build(HTML_DIR + "securities-add.ftl", out);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("type") String type,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam(SECURITY_XML) String securityXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {
    
    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    FlexiBean out = createRootData();
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    out.put("requestScheme", scheme);
    
    type = StringUtils.defaultString(StringUtils.trimToNull(type));
    switch (type) {
      case "xml":
        securityXml = StringUtils.trimToNull(securityXml);
        if (securityXml == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        ManageableSecurity security = addSecurity(securityXml, uniqueIdScheme);
        out.put("addedSecurities", getAddedSecurityId(security));
        break;
      case StringUtils.EMPTY: // create security by ID if type is missing
      case "id":
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        if (idScheme == null || idValue == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        Collection<ExternalIdBundle> requestBundles = buildSecurityRequest(scheme, idValue);
        SecurityLoaderResult loaderResult = data().getSecurityLoader().loadSecurities(SecurityLoaderRequest.create(requestBundles));
        out.put("addedSecurities", getLoadedSecuritiesId(loaderResult.getResultMap(), requestBundles, scheme));
        break;
      default:
        throw new IllegalArgumentException("Can only add security by XML or ID");
    }    
    return Response.ok(getFreemarker().build(JSON_DIR + "securities-added.ftl", out)).build();
  }
    
  private ManageableSecurity addSecurity(String securityXml, String uniqueIdScheme) {
    Bean securityBean = JodaBeanSerialization.deserializer().xmlReader().read(securityXml);
    SecurityMaster securityMaster = data().getSecurityMaster();
    ManageableSecurity manageableSecurity = (ManageableSecurity) securityBean;
    if (uniqueIdScheme != null) {
      manageableSecurity.setUniqueId(UniqueId.of(uniqueIdScheme, uniqueIdScheme));
    }
    SecurityDocument addedSecDoc = securityMaster.add(new SecurityDocument(manageableSecurity));
    return addedSecDoc.getSecurity();
  }

  private Map<String, String> getAddedSecurityId(ManageableSecurity security) {
    Map<String, String> addedSecurities = new HashMap<>();
    ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    UniqueId uniqueId = security.getUniqueId();
    String objectIdentifier = uniqueId.getObjectId().toString();
    String externalIdValue = StringUtils.EMPTY;
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
  public String getMetaDataJSON(@QueryParam("uniqueIdScheme") String uniqueIdScheme) {
    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    FlexiBean out = super.createRootData();
    out.put("schemaVersion", getSecurityMasterSchemaVersion(uniqueIdScheme));
    out.put("securityTypes", data().getSecurityTypes().values());
    out.put("description2type", data().getSecurityTypes());
    return getFreemarker().build(JSON_DIR + "metadata.ftl", out);
  }

  private String getSecurityMasterSchemaVersion(String uniqueIdScheme) {
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setUniqueIdScheme(uniqueIdScheme);
    request.setSchemaVersion(true);
    request.setSecurityTypes(false);
    SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(request);
    return metaData.getSchemaVersion();
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
        throw ex;
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
    request.setSecurityTypes(false);
    
    if (data().getSecurityMaster() instanceof DelegatingSecurityMaster) {
      Map<String, String> schemaVersionByScheme = new HashMap<>();
      
      DelegatingSecurityMaster delegatingSecMaster = (DelegatingSecurityMaster) data().getSecurityMaster();
      Map<String, SecurityMaster> delegates = delegatingSecMaster.getDelegates();
      for (Entry<String, SecurityMaster> entry : delegates.entrySet()) {
        SecurityMaster securityMaster = entry.getValue();
        SecurityMetaDataResult metaData = securityMaster.metaData(request);
        schemaVersionByScheme.put(entry.getKey(), metaData.getSchemaVersion());
      }
      out.put("schemaVersionByScheme", schemaVersionByScheme);
      out.put("uniqueIdSchemes", schemaVersionByScheme.keySet());
    } else {
      SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(request);
      out.put("schemaVersion", metaData.getSchemaVersion());
    }
    out.put("description2type", data().getSecurityTypes());
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
