/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.orgs;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all organizations.
 * <p>
 * The organizations resource represents the whole of an organization master.
 */
@Path("/organizations")
public class WebOrganizationsResource extends AbstractWebOrganizationResource {
  
  /**
   * Creates the resource.
   * @param organizationMaster  the organization master, not null
   */
  public WebOrganizationsResource(final OrganizationMaster organizationMaster) {
    super(organizationMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.SECURITY)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("shortName") String shortName,
      @QueryParam("obligorTicker") String obligorTicker,
      @QueryParam("obligorREDCode") String obligorREDCode,
      @QueryParam("organizationId") List<String> organizationIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, shortName, obligorTicker, obligorREDCode, organizationIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "organizations.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.SECURITY)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("shortName") String shortName,
      @QueryParam("obligorTicker") String obligorTicker,
      @QueryParam("obligorREDCode") String obligorREDCode,
      @QueryParam("organizationId") List<String> organizationIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, shortName, obligorTicker, obligorREDCode, organizationIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "organizations.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final String shortName, final String obligorTicker,
      final String obligorREDCode, final List<String> organizationIdStrs, final UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    OrganizationSearchRequest searchRequest = new OrganizationSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setObligorShortName(StringUtils.trimToNull(shortName));
    searchRequest.setObligorTicker(StringUtils.trimToNull(obligorTicker));
    searchRequest.setObligorREDCode(StringUtils.trimToNull(obligorREDCode));
    for (String organizationIdStr : organizationIdStrs) {
      searchRequest.addOrganizationObjectId(ObjectId.parse(organizationIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      OrganizationSearchResult searchResult = data().getOrganizationMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }


  //-------------------------------------------------------------------------
  @Path("{organizationId}")
  public WebOrganizationResource findOrganization(@Subscribe @PathParam("organizationId") String idStr) {
    data().setUriOrganizationId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      OrganizationDocument doc = data().getOrganizationMaster().get(oid);
      data().setOrganization(doc);
    } catch (DataNotFoundException ex) {
      OrganizationHistoryRequest historyRequest = new OrganizationHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      OrganizationHistoryResult historyResult = data().getOrganizationMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setOrganization(historyResult.getFirstDocument());
    }
    return new WebOrganizationResource(this);
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
  public static URI uri(WebOrganizationsData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebOrganizationsData data, ExternalIdBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebOrganizationsResource.class);
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
