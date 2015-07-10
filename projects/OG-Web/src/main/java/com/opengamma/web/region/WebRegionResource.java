/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import static com.opengamma.util.EnumUtils.safeValueOf;

import java.net.URI;

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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.ZoneId;

import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;

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
  public String getHTML() {
    RegionSearchRequest search = new RegionSearchRequest();
    search.setPagingRequest(PagingRequest.ALL);  // may need to add paging
    search.setChildrenOfId(data().getRegion().getUniqueId());
    RegionSearchResult children = data().getRegionMaster().search(search);
    data().setRegionChildren(children.getDocuments());
    
    for (UniqueId parentId : data().getRegion().getRegion().getParentRegionIds()) {
      RegionDocument parent = data().getRegionMaster().get(parentId);
      data().getRegionParents().add(parent);
    }
    
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "region.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    RegionSearchRequest search = new RegionSearchRequest();
    search.setPagingRequest(PagingRequest.ALL);  // may need to add paging
    search.setChildrenOfId(data().getRegion().getUniqueId());
    RegionSearchResult children = data().getRegionMaster().search(search);
    data().setRegionChildren(children.getDocuments());

    for (UniqueId parentId : data().getRegion().getRegion().getParentRegionIds()) {
      RegionDocument parent = data().getRegionMaster().get(parentId);
      data().getRegionParents().add(parent);
    }

    FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "region.ftl", out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") String classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    if (data().getRegion().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    RegionClassification regionClassification = safeValueOf(RegionClassification.class, classification);
    if (name == null || regionClassification == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (regionClassification == null) {
        out.put("err_classificationMissing", true);
      }
      String html = getFreemarker().build(HTML_DIR + "region-add.ftl", out);
      return Response.ok(html).build();
    }
    if (fullName == null) {
      fullName = name;
    }
    URI uri = addRegion(name, fullName, regionClassification, countryISO, currencyISO, timeZoneId);
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") RegionClassification classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    if (data().getRegion().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    if (name == null || classification == null) {
      Response.status(Status.BAD_REQUEST);
    }
    if (fullName == null) {
      fullName = name;
    }
    URI uri = addRegion(name, fullName, classification, countryISO, currencyISO, timeZoneId);
    return Response.created(uri).build();
  }

  private URI addRegion(String name, String fullName, RegionClassification classification, String countryISO, String currencyISO, String timeZoneId) {
    ManageableRegion region = new ManageableRegion();
    region.getParentRegionIds().add(data().getRegion().getUniqueId());
    region.setName(name);
    region.setFullName(fullName);
    region.setClassification(classification);
    region.setCountry(countryISO != null ? Country.of(countryISO) : null);
    region.setCurrency(currencyISO != null ? Currency.of(currencyISO) : null);
    region.setTimeZone(timeZoneId != null ? ZoneId.of(timeZoneId) : null);
    RegionDocument doc = new RegionDocument(region);
    RegionDocument added = data().getRegionMaster().add(doc);
    return WebRegionResource.uri(data(), added.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") String classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    if (data().getRegion().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    RegionClassification regionClassification = safeValueOf(RegionClassification.class, classification);
    if (name == null || regionClassification == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (regionClassification == null) {
        out.put("err_classificationMissing", true);
      }
      String html = getFreemarker().build(HTML_DIR + "region-update.ftl", out);
      return Response.ok(html).build();
    }
    if (fullName == null) {
      fullName = name;
    }
    URI uri = updateRegion(name, fullName, regionClassification, countryISO, currencyISO, timeZoneId);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("fullname") String fullName,
      @FormParam("classification") String classification,
      @FormParam("country") String countryISO,
      @FormParam("currency") String currencyISO,
      @FormParam("timezone") String timeZoneId) {
    if (data().getRegion().isLatest() == false) {  // TODO: idempotent
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    fullName = StringUtils.trimToNull(fullName);
    countryISO = StringUtils.trimToNull(countryISO);
    currencyISO = StringUtils.trimToNull(currencyISO);
    timeZoneId = StringUtils.trimToNull(timeZoneId);
    RegionClassification regionClassification = safeValueOf(RegionClassification.class, classification);
    if (name == null || regionClassification == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    if (fullName == null) {
      fullName = name;
    }
    updateRegion(name, fullName, regionClassification, countryISO, currencyISO, timeZoneId);
    return Response.ok().build();
  }

  private URI updateRegion(String name, String fullName, RegionClassification classification, String countryISO, String currencyISO, String timeZoneId) {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(data().getRegion().getUniqueId());
    region.setParentRegionIds(data().getRegion().getRegion().getParentRegionIds());
    region.setName(name);
    region.setFullName(fullName);
    region.setClassification(classification);
    region.setCountry(countryISO != null ? Country.of(countryISO) : null);
    region.setCurrency(currencyISO != null ? Currency.of(currencyISO) : null);
    region.setTimeZone(timeZoneId != null ? ZoneId.of(timeZoneId) : null);
    RegionDocument doc = new RegionDocument(region);
    doc = data().getRegionMaster().update(doc);
    data().setRegion(doc);
    return WebRegionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    RegionDocument doc = data().getRegion();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    data().getRegionMaster().remove(doc.getUniqueId());
    URI uri = WebRegionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    RegionDocument doc = data().getRegion();
    if (doc.isLatest()) {  // idempotent
      data().getRegionMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    RegionDocument doc = data().getRegion();
    out.put("regionDoc", doc);
    out.put("region", doc.getRegion());
    out.put("deleted", !doc.isLatest());
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
  public static URI uri(final WebRegionData data, final UniqueId overrideRegionId) {
    String regionId = data.getBestRegionUriId(overrideRegionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebRegionResource.class).build(regionId);
  }

}
