/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Year;

import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "holiday.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getHTML(@Context Request request) {
    EntityTag etag = new EntityTag(data().getHoliday().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    String json = getFreemarker().build(JSON_DIR + "holiday.ftl", out);
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
    HolidayDocument doc = data().getHoliday();
    out.put("holidayDoc", doc);
    out.put("holiday", doc.getHoliday());
    out.put("deleted", !doc.isLatest());
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
        map.add(Pairs.of(Year.of(year), dates.subList(start, pos)));
        year = dates.get(pos).getYear();
        start = pos;
      }
      map.add(Pairs.of(Year.of(year), dates.subList(start, pos)));
    }
    out.put("holidayDatesByYear", map);
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebHolidayVersionsResource findVersions() {
    return new WebHolidayVersionsResource(this);
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
   * @param overrideHolidayId  the override holiday id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data, final UniqueId overrideHolidayId) {
    String holidayId = data.getBestHolidayUriId(overrideHolidayId);
    return data.getUriInfo().getBaseUriBuilder().path(WebHolidayResource.class).build(holidayId);
  }

}
