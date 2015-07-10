/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import java.net.URI;
import java.util.List;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.LegalEntitySearchSortOrder;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all legalEntity documents.
 * <p/>
 * The legalEntity documents resource represents all the data for one element type in the legalEntity master.
 */
@Path("/legalentities")
public class WebLegalEntitiesResource extends AbstractWebLegalEntityResource {

  /**
   * Creates the resource.
   *
   * @param legalEntityMaster the legalEntity master, not null
   * @param securityMaster the securityMaster master, not null
   */
  public WebLegalEntitiesResource(final LegalEntityMaster legalEntityMaster, final SecurityMaster securityMaster) {
    super(legalEntityMaster, securityMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.CONFIG)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("legalEntityId") List<String> legalEntityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    LegalEntitySearchSortOrder so = buildSortOrder(sort, LegalEntitySearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, type, legalEntityIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "legalentities.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.CONFIG)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("legalEntityId") List<String> legalEntityIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    LegalEntitySearchSortOrder so = buildSortOrder(sort, LegalEntitySearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, type, legalEntityIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "legalentities.ftl", out);
  }

  private FlexiBean search(PagingRequest request, LegalEntitySearchSortOrder so, String name,
                           String typeName, List<String> legalEntityIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();

    LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest();
    typeName = StringUtils.trimToNull(typeName);
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", typeName);
    for (String legalEntityIdStr : legalEntityIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(legalEntityIdStr));
    }

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      LegalEntitySearchResult searchResult = data().getLegalEntityMaster().search(searchRequest);
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
      @FormParam("name") String name,
      @FormParam("legalEntityXML") String xml,
      @FormParam("type") String typeName) {
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    typeName = StringUtils.trimToNull(typeName);

    if (name == null || xml == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      out.put("name", StringUtils.defaultString(name));
      out.put("type", StringUtils.defaultString(typeName));
      out.put("legalEntityXML", StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(xml)));
      String html = getFreemarker().build(HTML_DIR + "legalentity-add.ftl", out);
      return Response.ok(html).build();
    }

    final ManageableLegalEntity legalEntity = parseXML(xml, ManageableLegalEntity.class);
    legalEntity.setName(name);
    LegalEntityDocument doc = new LegalEntityDocument(legalEntity);
    LegalEntityDocument added = data().getLegalEntityMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("legalEntityJSON") String json,
      @FormParam("legalEntityXML") String xml,
      @FormParam("type") String typeName) {
    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    typeName = StringUtils.trimToNull(typeName);

    final Class<? extends ManageableLegalEntity> typeClazz = (typeName != null ? data().getTypeMap().get(typeName) : null);
    Response result = null;
    if (name == null || typeClazz == null || isEmptyLegalEntityData(json, xml)) {
      result = Response.status(Status.BAD_REQUEST).build();
    } else {
      ManageableLegalEntity legalEntity = null;
      if (json != null) {
        legalEntity = (ManageableLegalEntity) parseJSON(json);
      } else if (xml != null) {
        legalEntity = parseXML(xml, typeClazz);
      }
      if (legalEntity == null) {
        result = Response.status(Status.BAD_REQUEST).build();
      } else {
        legalEntity.setName(name);
        LegalEntityDocument doc = new LegalEntityDocument(legalEntity);
        LegalEntityDocument added = data().getLegalEntityMaster().add(doc);
        URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
        result = Response.created(uri).build();
      }
    }
    return result;
  }

  private boolean isEmptyLegalEntityData(String json, String xml) {
    return (json == null && xml == null);
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
  @Path("{legalEntityId}")
  public Object findLegalEntityHTML(@Subscribe @PathParam("legalEntityId") String idStr) {
    data().setUriLegalEntityId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      LegalEntityDocument doc = data().getLegalEntityMaster().get(oid);
      data().setLegalEntity(doc);
    } catch (DataNotFoundException ex) {
      LegalEntityHistoryRequest historyRequest = new LegalEntityHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      LegalEntityHistoryResult historyResult = data().getLegalEntityMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setLegalEntity(historyResult.getFirstDocument());
    }
    return new WebLegalEntityResource(this);
  }

  //-------------------------------------------------------------------------

  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for legalEntities.
   *
   * @param data the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebLegalEntityData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebLegalEntitiesResource.class);
    return builder.build();
  }

}
