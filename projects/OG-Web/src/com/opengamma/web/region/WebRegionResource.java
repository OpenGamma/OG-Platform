/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;

import javax.time.calendar.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.common.Currency;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * RESTful resource for a region.
 */
@Path("/regions/{regionId}")
public class WebRegionResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebRegionResource(final AbstractWebRegionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    RegionSearchRequest search = new RegionSearchRequest();
    search.setPagingRequest(PagingRequest.ALL);  // may need to add paging
    search.setChildrenOfId(data().getRegion().getUniqueId());
    RegionSearchResult children = data().getRegionMaster().search(search);
    data().setRegionChildren(children.getDocuments());
    
    for (UniqueIdentifier parentId : data().getRegion().getRegion().getParentRegionIds()) {
      RegionDocument parent = data().getRegionMaster().get(parentId);
      data().getRegionParents().add(parent);
    }
    
    FlexiBean out = createRootData();
    return getFreemarker().build("regions/region.ftl", out);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") RegionClassification classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    if (name == null || classification == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (classification == null) {
        out.put("err_classificationMissing", true);
      }
      String html = getFreemarker().build("regions/region-add.ftl", out);
      return Response.ok(html).build();
    }
    if (fullName == null) {
      fullName = name;
    }
    ManageableRegion region = new ManageableRegion();
    region.getParentRegionIds().add(data().getRegion().getUniqueId());
    region.setName(name);
    region.setFullName(fullName);
    region.setClassification(classification);
    region.setCountryISO(countryISO);
    region.setCurrency(currencyISO != null ? Currency.getInstance(currencyISO) : null);
    region.setTimeZone(timeZoneId != null ? TimeZone.of(timeZoneId) : null);
    RegionDocument doc = new RegionDocument(region);
    RegionDocument added = data().getRegionMaster().add(doc);
    URI uri = WebRegionResource.uri(data(), added.getUniqueId());
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") RegionClassification classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    if (name == null || classification == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (classification == null) {
        out.put("err_classificationMissing", true);
      }
      String html = getFreemarker().build("regions/region-update.ftl", out);
      return Response.ok(html).build();
    }
    if (fullName == null) {
      fullName = name;
    }
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(data().getRegion().getUniqueId());
    region.setParentRegionIds(data().getRegion().getRegion().getParentRegionIds());
    region.setName(name);
    region.setFullName(fullName);
    region.setClassification(classification);
    region.setCountryISO(countryISO);
    region.setCurrency(currencyISO != null ? Currency.getInstance(currencyISO) : null);
    region.setTimeZone(timeZoneId != null ? TimeZone.of(timeZoneId) : null);
    RegionDocument doc = new RegionDocument(region);
    doc = data().getRegionMaster().update(doc);
    data().setRegion(doc);
    URI uri = WebRegionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    RegionDocument doc = data().getRegion();
    data().getRegionMaster().remove(doc.getUniqueId());
    URI uri = WebRegionsResource.uri(data());
    return Response.seeOther(uri).build();
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
    out.put("regionParents", data().getRegionParents());
    out.put("regionChildren", data().getRegionChildren());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebRegionVersionsResource findVersions() {
    return new WebRegionVersionsResource(this);
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
   * @param overrideRegionId  the override region id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data, final UniqueIdentifier overrideRegionId) {
    String regionId = data.getBestRegionUriId(overrideRegionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebRegionResource.class).build(regionId);
  }

}
