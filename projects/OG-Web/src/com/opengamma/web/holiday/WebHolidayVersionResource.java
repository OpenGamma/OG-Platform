/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.tuple.Pair;

/**
 * RESTful resource for a version of a holiday.
 */
@Path("/holidays/{holidayId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebHolidayVersionResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHolidayVersionResource(final AbstractWebHolidayResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
//  @GET
//  public String getHTML() {
//    FlexiBean out = createRootData();
//    return getFreemarker().build("holidays/holidayversion.ftl", out);
//  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    String json = getFreemarker().build("holidays/jsonholiday.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HolidayDocument latestDoc = data().getHoliday();
    HolidayDocument versionedHoliday = data().getVersioned();
    out.put("latestHolidayDoc", latestDoc);
    out.put("latestHoliday", latestDoc.getHoliday());
    out.put("holidayDoc", versionedHoliday);
    out.put("holiday", versionedHoliday.getHoliday());
    out.put("deleted", !latestDoc.isLatest());
    List<Pair<Year, List<LocalDate>>> map = new ArrayList<Pair<Year, List<LocalDate>>>();
    List<LocalDate> dates = versionedHoliday.getHoliday().getHolidayDates();
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
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data, final UniqueIdentifier overrideVersionId) {
    String holidayId = data.getBestHolidayUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebHolidayVersionResource.class).build(holidayId, versionId);
  }

}
