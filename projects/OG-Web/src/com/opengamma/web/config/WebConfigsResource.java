/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.BiMap;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.json.JSONBuilder;
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
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("configId") List<String> configIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    ConfigSearchSortOrder so = buildSortOrder(sort, ConfigSearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, type, configIdStrs, uriInfo);
    return getFreemarker().build("configs/configs.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("configId") List<String> configIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    ConfigSearchSortOrder so = buildSortOrder(sort, ConfigSearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, type, configIdStrs, uriInfo);
    return getFreemarker().build("configs/jsonconfigs.ftl", out);
  }

  @SuppressWarnings("unchecked")
  private FlexiBean search(PagingRequest request, ConfigSearchSortOrder so, String name,
      String type, List<String> configIdStrs, UriInfo uriInfo) {
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
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", type);
    for (String configIdStr : configIdStrs) {
      searchRequest.addConfigId(ObjectId.parse(configIdStr));
    }
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      ConfigSearchResult<Object> searchResult = null;
      if (searchRequest.getType() != null) {
        searchResult = data().getConfigMaster().search(searchRequest);
      } else {
        searchResult = new ConfigSearchResult<Object>();
        searchResult.setPaging(Paging.of(searchRequest.getPagingRequest(), searchResult.getDocuments()));
      }
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
      @FormParam("configxml") String xml,
      @FormParam("type") String type) {
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    type = StringUtils.trimToNull(type);
    
    if (name == null || xml == null || type == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      if (type == null) {
        out.put("err_typeMissing", true);
      }
      String html = getFreemarker().build("configs/config-add.ftl", out);
      return Response.ok(html).build();
    }
     
    final Object configObj = parseXML(xml);
    final Class<?> logicalClazz = getLogicalClazz(type);
    
    ConfigDocument<Object> doc = new ConfigDocument<Object>(logicalClazz);
    doc.setName(name);
    doc.setValue(configObj);
    ConfigDocument<?> added = data().getConfigMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  private Class<?> getLogicalClazz(final String type) {
    final Class<?> logicalClass;
    try {
      logicalClass = Class.forName(type);
    } catch (Throwable t) {
      throw new OpenGammaRuntimeException("Invalid logical class name from form param type[" + type + "]");
    }
    return logicalClass;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("configJSON") String json,
      @FormParam("configXML") String xml,
      @FormParam("type") String type) {
    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    type = StringUtils.trimToNull(type);
    Response result = null;
    if (name == null || type == null || isEmptyConfigData(json, xml)) {
      result = Response.status(Status.BAD_REQUEST).build();
    } else {
      Object configObj = null;
      if (json != null) {
        configObj = parseJSON(json);
      } else if (xml != null) {
        configObj = parseXML(xml);
      }
      if (configObj == null) {
        result = Response.status(Status.BAD_REQUEST).build();
      } else {
        final Class<?> logicalClazz = getLogicalClazz(type);
        ConfigDocument<Object> doc = new ConfigDocument<Object>(logicalClazz);
        doc.setName(name);
        doc.setValue(configObj);
        ConfigDocument<?> added = data().getConfigMaster().add(doc);
        URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
        result = Response.created(uri).build();
      }
    }
    return result;
  }

  private boolean isEmptyConfigData(String json, String xml) {
    return (json == null && xml == null);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("configs/jsonmetadata.ftl", out);
  }
  
  //-------------------------------------------------------------------------
  @GET
  @Path("templates/{configType}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getTemplateJSON(@PathParam("configType") String configType) {
    BiMap<String, Class<?>> typeMap = data().getTypeMap();
    Class<?> typeClazz = typeMap.get(configType);
    String template = null;
    if (typeClazz != null) {
      JSONBuilder<?> jsonBuilder = data().getJsonBuilderMap().get(typeClazz);
      if (jsonBuilder != null) {
        template = jsonBuilder.getTemplate();
      }
    } 
    FlexiBean out = super.createRootData();
    out.put("template", template);
    out.put("type", configType);
    return getFreemarker().build("configs/jsontemplate.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{configId}")
  public WebConfigResource findConfig(@PathParam("configId") String idStr) {
    data().setUriConfigId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
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
    out.put("curveSpecs", CurveSpecificationBuilderConfiguration.s_curveSpecNames);
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
