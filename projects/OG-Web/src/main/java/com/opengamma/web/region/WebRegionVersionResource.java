/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.paging.PagingRequest;

/**
 * RESTful resource for a version of a region.
 */
@Path("/regions/{regionId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebRegionVersionResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebRegionVersionResource(final AbstractWebRegionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "regionversion.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    RegionSearchRequest search = new RegionSearchRequest();
    search.setPagingRequest(PagingRequest.ALL);  // may need to add paging
    search.setChildrenOfId(data().getVersioned().getUniqueId());
    RegionSearchResult children = data().getRegionMaster().search(search);
    data().setRegionChildren(children.getDocuments());

    for (UniqueId parentId : data().getRegion().getRegion().getParentRegionIds()) {
      RegionDocument parent = data().getRegionMaster().get(parentId);
      data().getRegionParents().add(parent);
    }
    
    FlexiBean out = createRootData();
    String json = getFreemarker().build(JSON_DIR + "region.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    RegionDocument latestDoc = data().getRegion();
    RegionDocument versionedRegion = data().getVersioned();
    out.put("latestRegionDoc", latestDoc);
    out.put("latestRegion", latestDoc.getRegion());
    out.put("regionDoc", versionedRegion);
    out.put("region", versionedRegion.getRegion());
    out.put("deleted", !latestDoc.isLatest());
    out.put("regionParents", data().getRegionParents());
    out.put("regionChildren", data().getRegionChildren());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data, final UniqueId overrideVersionId) {
    String regionId = data.getBestRegionUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebRegionVersionResource.class).build(regionId, versionId);
  }

}
