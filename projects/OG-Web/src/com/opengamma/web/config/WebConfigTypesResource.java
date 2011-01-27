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
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all configuration documents.
 * <p>
 * The configuration documents resource represents all the data for one element type in the config master.
 * 
 * @param <T>  the config element type
 */
@Path("/configs/{type}")
public class WebConfigTypesResource<T> extends AbstractWebConfigTypeResource<T> {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigTypesResource(final AbstractWebConfigResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      ConfigSearchResult<T> searchResult = data().getConfigTypeMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return getFreemarker().build("configs/configtypes.ftl", out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      String html = getFreemarker().build("configs/configtypes-add.ftl", out);
      return Response.ok(html).build();
    }
    ConfigDocument<T> doc = new ConfigDocument<T>();
    doc.setName(name);
    // doc.setValue((T) "PLACEHOLDER");
    ConfigDocument<T> added = data().getConfigTypeMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{configId}")
  public WebConfigTypeResource<T> findConfig(@PathParam("configId") String idStr) {
    data().setUriConfigId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    try {
      ConfigDocument<T> doc = data().getConfigTypeMaster().get(oid);
      data().setConfig(doc);
    } catch (DataNotFoundException ex) {
      ConfigHistoryRequest historyRequest = new ConfigHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      ConfigHistoryResult<T> historyResult = data().getConfigTypeMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setConfig(historyResult.getFirstDocument());
    }
    return new WebConfigTypeResource<T>(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for configs.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebConfigData<?> data) {
    return uri(data, (String) null);
  }

  /**
   * Builds a URI for configs.
   * @param data  the data, not null
   * @param overrideType  the override type, may be null
   * @return the URI, not null
   */
  public static URI uri(WebConfigData<?> data, Class<?> overrideType) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebConfigTypesResource.class);
    String typeStr = data.getTypeMap().inverse().get(overrideType != null ? overrideType : data.getType());
    return builder.build(typeStr);
  }

  /**
   * Builds a URI for configs.
   * @param data  the data, not null
   * @param overrideType  the override type, may be null
   * @return the URI, not null
   */
  public static URI uri(WebConfigData<?> data, String overrideType) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebConfigTypesResource.class);
    String typeStr = overrideType != null ? overrideType : data.getTypeMap().inverse().get(data.getType());
    return builder.build(typeStr);
  }

}
