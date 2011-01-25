/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Year;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;
import org.json.JSONObject;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.Pair;

/**
 * RESTful resource for a holiday.
 */
@Path("/holidays/{holidayId}")
public class WebHolidayResource extends AbstractWebHolidayResource {
  
  private static final String TEMPLATE_DATA_KEY = "templateData";
  private static final String COUNTRY_NAME_KEY = "COUNTRY_NAME";
  private static final String DATES_KEY = "dates";
  

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
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return buildJSONOutput(out);
  }

  private String buildJSONOutput(FlexiBean out) {
    Map<String, Object> jsonMap = new HashMap<String, Object>();
    HolidayDocument doc = (HolidayDocument) out.get("holidayDoc");
    @SuppressWarnings("unchecked")
    List<Pair<Year, List<LocalDate>>> holidayDatesByYear = (List<Pair<Year, List<LocalDate>>>) out.get("holidayDatesByYear");
    
    jsonMap.put(TEMPLATE_DATA_KEY, createTemplateData(doc));
    jsonMap.put(DATES_KEY, createDatesData(holidayDatesByYear));
    
    return new JSONObject(jsonMap).toString();
  }

  private Map<Integer, List<String>> createDatesData(List<Pair<Year, List<LocalDate>>> holidayDatesByYear) {
    Map<Integer, List<String>> datesData = new TreeMap<Integer, List<String>>();
    for (Pair<Year, List<LocalDate>> item : holidayDatesByYear) {
      Year year = item.getFirst();
      List<LocalDate> dates = item.getSecond();
      datesData.put(year.getValue(), convertDates(dates));
    }
    return datesData;
  }

  private List<String> convertDates(List<LocalDate> dates) {
    List<String> results = new ArrayList<String>();
    for (LocalDate date : dates) {
      results.add(formatDate(date));
    }
    return results;
  }

  private String formatDate(LocalDate date) {
    return DateUtil.printMMDD(date);
  }

  private Map<String, String> createTemplateData(HolidayDocument doc) {
    Map<String, String> templateData = new HashMap<String, String>();
    templateData.put(COUNTRY_NAME_KEY, doc.getName());
    return templateData;
  }

//  @PUT
//  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//  public Response put(
//      @FormParam("name") String name,
//      @FormParam("idscheme") String idScheme,
//      @FormParam("idvalue") String idValue,
//      @FormParam("regionscheme") String regionScheme,
//      @FormParam("regionvalue") String regionValue) {
//    if (data().getHoliday().isLatest() == false) {
//      return Response.status(Status.FORBIDDEN).entity(get()).build();
//    }
//    
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
//    if (doc.isLatest() == false) {
//      return Response.status(Status.FORBIDDEN).entity(get()).build();
//    }
//    
//    data().getHolidayMaster().removeHoliday(doc.getHolidayId());
//    URI uri = WebHolidayResource.uri(data());
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
