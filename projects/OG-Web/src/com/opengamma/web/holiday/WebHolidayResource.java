/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Year;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.tuple.Pair;

/**
 * RESTful resource for a holiday.
 */
@Path("/holidays/{holidayId}")
public class WebHolidayResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHolidayResource(final AbstractWebHolidayResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("holidays/holiday.ftl", out);
  }

//  @PUT
//  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//  public Response put(
//      @FormParam("name") String name,
//      @FormParam("idscheme") String idScheme,
//      @FormParam("idvalue") String idValue,
//      @FormParam("regionscheme") String regionScheme,
//      @FormParam("regionvalue") String regionValue) {
//    name = StringUtils.trimToNull(name);
//    idScheme = StringUtils.trimToNull(idScheme);
//    idValue = StringUtils.trimToNull(idValue);
//    if (name == null || idScheme == null || idValue == null) {
//      FlexiBean out = createRootData();
//      if (name == null) {
//        out.put("err_nameMissing", true);
//      }
//      if (idScheme == null) {
//        out.put("err_idschemeMissing", true);
//      }
//      if (idValue == null) {
//        out.put("err_idvalueMissing", true);
//      }
//      if (regionScheme == null) {
//        out.put("err_regionschemeMissing", true);
//      }
//      if (regionValue == null) {
//        out.put("err_regionvalueMissing", true);
//      }
//      String html = getFreemarker().build("holidays/holiday-update.ftl", out);
//      return Response.ok(html).build();
//    }
//    Holiday holiday = data().getHoliday().getHoliday().clone();
//    holiday.setName(name);
//    holiday.setIdentifiers(IdentifierBundle.of(Identifier.of(idScheme, idValue)));
//    holiday.setRegionId(Identifier.of(regionScheme, regionValue));
//    HolidayDocument doc = new HolidayDocument(holiday);
//    doc = data().getHolidayMaster().updateHoliday(doc);
//    data().setHoliday(doc);
//    URI uri = WebHolidayResource.uri(data());
//    return Response.seeOther(uri).build();
//  }
//
//  @DELETE
//  public Response delete() {
//    HolidayDocument doc = data().getHoliday();
//    data().getHolidayMaster().removeHoliday(doc.getHolidayId());
//    URI uri = WebHolidaysResource.uri(data());
//    return Response.seeOther(uri).build();
//  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HolidayDocument doc = data().getHoliday();
    out.put("holidayDoc", doc);
    out.put("holiday", doc.getHoliday());
    List<Pair<Year, List<LocalDate>>> map = new ArrayList<Pair<Year, List<LocalDate>>>();
    List<LocalDate> dates = doc.getHoliday().getHolidayDates();
    if (dates.size() > 0) {
      int year = dates.get(0).getYear();
      int start = 0;
      int pos = 0;
      for ( ; pos < dates.size(); pos++) {
        if (dates.get(pos).getYear() == year) {
          continue;
        }
        map.add(Pair.of(Year.of(year), dates.subList(start, pos)));
        year = dates.get(pos).getYear();
        start = pos;
      }
      map.add(Pair.of(Year.of(year), dates.subList(start, pos)));
    }
    out.put("holidayDatesByYear", map);
    return out;
  }

  //-------------------------------------------------------------------------
//  @Path("versions")
//  public WebHolidayVersionsResource findVersions() {
//    return new WebHolidayVersionsResource(this);
//  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideHolidayId  the override holiday id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data, final UniqueIdentifier overrideHolidayId) {
    String holidayId = data.getBestHolidayUriId(overrideHolidayId);
    return data.getUriInfo().getBaseUriBuilder().path(WebHolidayResource.class).build(holidayId);
  }

}
