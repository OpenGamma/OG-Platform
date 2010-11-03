/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.world.region.master.RegionDocument;
import com.opengamma.financial.world.region.master.RegionHistoryRequest;
import com.opengamma.financial.world.region.master.RegionHistoryResult;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for all versions of an region.
 */
@Path("/regions/{regionId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebRegionVersionsResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebRegionVersionsResource(final AbstractWebRegionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    RegionHistoryRequest request = new RegionHistoryRequest();
    request.setRegionId(data().getRegion().getRegionId());
    RegionHistoryResult result = data().getRegionMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getRegions());
    return getFreemarker().build("regions/regionversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    RegionDocument doc = data().getRegion();
    out.put("regionDoc", doc);
    out.put("region", doc.getRegion());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebRegionVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    RegionDocument doc = data().getRegion();
    UniqueIdentifier combined = doc.getRegionId().withVersion(idStr);
    if (doc.getRegionId().equals(combined) == false) {
      RegionDocument versioned = data().getRegionMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebRegionVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data) {
    String regionId = data.getBestRegionUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebRegionVersionsResource.class).build(regionId);
  }

}
