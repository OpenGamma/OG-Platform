/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.HolidayMaster;
import com.opengamma.financial.world.holiday.master.HolidaySearchRequest;
import com.opengamma.financial.world.holiday.master.HolidaySearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for all holidays.
 * <p>
 * The holidays resource represents the whole of a holiday master.
 */
@Path("/holidays")
public class WebHolidaysResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param holidayMaster  the holiday master, not null
   */
  public WebHolidaysResource(final HolidayMaster holidayMaster) {
    super(holidayMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("currency") String currencyISO,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    if (StringUtils.isNotEmpty(type)) {
      searchRequest.setType(HolidayType.valueOf(type));
    }
    if (currencyISO != null) {
      searchRequest.setCurrency(Currency.getInstance(currencyISO));
    }
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      if (HolidayType.BANK.name().equals(type)) {
        IdentifierBundle old = (searchRequest.getRegionIdentifiers() != null ? searchRequest.getRegionIdentifiers() : IdentifierBundle.EMPTY);
        searchRequest.setRegionIdentifiers(old.withIdentifier(id));
      } else { // assume settlement/trading
        IdentifierBundle old = (searchRequest.getExchangeIdentifiers() != null ? searchRequest.getExchangeIdentifiers() : IdentifierBundle.EMPTY);
        searchRequest.setExchangeIdentifiers(old.withIdentifier(id));
      }
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      HolidaySearchResult searchResult = data().getHolidayMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return getFreemarker().build("holidays/holidays.ftl", out);
  }

//  //-------------------------------------------------------------------------
//  @POST
//  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//  public Response post(
//      @FormParam("name") String name,
//      @FormParam("idscheme") String idScheme,
//      @FormParam("idvalue") String idValue,
//      @FormParam("regionscheme") String regionScheme,
//      @FormParam("regionvalue") String regionValue) {
//    name = StringUtils.trimToNull(name);
//    idScheme = StringUtils.trimToNull(idScheme);
//    idValue = StringUtils.trimToNull(idValue);
//    regionScheme = StringUtils.trimToNull(regionScheme);
//    regionValue = StringUtils.trimToNull(regionValue);
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
//      String html = getFreemarker().build("holidays/holidays-add.ftl", out);
//      return Response.ok(html).build();
//    }
//    Identifier id = Identifier.of(idScheme, idValue);
//    Identifier region = Identifier.of(regionScheme, regionValue);
//    Holiday holiday = new Holiday(IdentifierBundle.of(id), name, region);
//    HolidayDocument doc = new HolidayDocument(holiday);
//    HolidayDocument added = data().getHolidayMaster().addHoliday(doc);
//    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getHolidayId().toLatest().toString()).build();
//    return Response.seeOther(uri).build();
//  }

  //-------------------------------------------------------------------------
  @Path("{holidayId}")
  public WebHolidayResource findHoliday(@PathParam("holidayId") String idStr) {
    data().setUriHolidayId(idStr);
    HolidayDocument holidayDoc = data().getHolidayMaster().get(UniqueIdentifier.parse(idStr));
    data().setHoliday(holidayDoc);
    return new WebHolidayResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for holidays.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebHolidayData data) {
    return uri(data, null, null);
  }

  /**
   * Builds a URI for holidays.
   * @param data  the data, not null
   * @param type  the holiday type, may be null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebHolidayData data, HolidayType type, IdentifierBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebHolidaysResource.class);
    if (type != null && identifiers != null) {
      builder.queryParam("type", type.name());
      Iterator<Identifier> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        Identifier id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
