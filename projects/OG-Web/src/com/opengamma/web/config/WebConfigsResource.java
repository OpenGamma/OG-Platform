/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;

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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all configuration documents.
 * <p>
 * The configuration documents resource represents all the data for one element type in the config master.
 * 
 */
@Path("/configs")
public class WebConfigsResource extends AbstractWebConfigResource {

  /**
   * Creates the resource.
   * @param configMaster  the config master, not null
   */
  public WebConfigsResource(final ConfigMaster configMaster) {
    super(configMaster);
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
    FlexiBean out = search(page, pageSize, name, type, uriInfo);
    return getFreemarker().build("configs/configs.ftl", out);
  }

  @SuppressWarnings("unchecked")
  private FlexiBean search(int page, int pageSize, String name, String type, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    @SuppressWarnings("rawtypes")
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    type = StringUtils.trimToNull(type);
    if (type != null) {
      Class<?> typeClazz = data().getTypeMap().get(type);
      searchRequest.setType(typeClazz);
    } else {
      searchRequest.setType(Object.class);
    }
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", type);
        
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      ConfigSearchResult<Object> searchResult = null;
      if (searchRequest.getType() != null) {
        searchResult = data().getConfigMaster().search(searchRequest);
      } else {
        searchResult = new ConfigSearchResult<Object>();
        searchResult.setPaging(Paging.of(searchResult.getDocuments(), searchRequest.getPagingRequest()));
      }
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    } 
    return out;
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @Context UriInfo uriInfo) {
    FlexiBean out = search(page, pageSize, name, type, uriInfo);
    return getFreemarker().build("configs/jsonconfigs.ftl", out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response post(
      @FormParam("name") String name,
      @FormParam("configxml") String xml) {
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      String html = getFreemarker().build("configs/config-add.ftl", out);
      return Response.ok(html).build();
    }
    Object configValue = parseXML(xml);
    ConfigDocument<Object> doc = new ConfigDocument<Object>(configValue.getClass());
    doc.setName(name);
    doc.setValue(configValue);
    ConfigDocument<?> added = data().getConfigMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("configxml") String xml) {
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    Object configValue = parseXML(xml);
    ConfigDocument<Object> doc = new ConfigDocument<Object>(configValue.getClass());
    doc.setName(name);
    doc.setValue(configValue);
    ConfigDocument<?> added = data().getConfigMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.created(uri).build();
  }
  
  
  //-------------------------------------------------------------------------
  @Path("{configId}")
  public WebConfigResource findConfig(@PathParam("configId") String idStr) {
    data().setUriConfigId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    try {
      ConfigDocument<?> doc = data().getConfigMaster().get(oid);
      data().setConfig(doc);
    } catch (DataNotFoundException ex) {
      ConfigHistoryRequest<Object> historyRequest = new ConfigHistoryRequest<Object>(oid, Object.class);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      ConfigHistoryResult<?> historyResult = data().getConfigMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setConfig(historyResult.getFirstDocument());
    }
    return new WebConfigResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigSearchRequest<Object> searchRequest = new ConfigSearchRequest<Object>();
    searchRequest.setType(Object.class);
    out.put("searchRequest", searchRequest);
    out.put("typeMap", data().getTypeMap());
    
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for configs.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebConfigData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebConfigsResource.class);
    return builder.build();
  }

}
