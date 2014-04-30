/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

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

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.HolidaySearchSortOrder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

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
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("currency") String currencyISO,
      @QueryParam("holidayId") List<String> holidayIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    HolidaySearchSortOrder so = buildSortOrder(sort, HolidaySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, type, currencyISO, holidayIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "holidays.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("currency") String currencyISO,
      @QueryParam("holidayId") List<String> holidayIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    HolidaySearchSortOrder so = buildSortOrder(sort, HolidaySearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, name, type, currencyISO, holidayIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "holidays.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, HolidaySearchSortOrder sort, String name, String type, String currencyISO,
      List<String> holidayIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(sort);
    searchRequest.setName(StringUtils.trimToNull(name));
    if (StringUtils.isNotEmpty(type)) {
      searchRequest.setType(HolidayType.valueOf(type));
    }
    if (currencyISO != null) {
      searchRequest.setCurrency(Currency.of(currencyISO));
    }
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      if (HolidayType.BANK.name().equals(type)) {
        searchRequest.addRegionExternalId(id);
      } else if (HolidayType.CUSTOM.name().equals(type)) {
        searchRequest.addCustomExternalId(id);
      } else { // assume settlement/trading
        searchRequest.addExchangeExternalId(id);
      }
    }
    for (String holidayIdStr : holidayIdStrs) {
      searchRequest.addHolidayObjectId(ObjectId.parse(holidayIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      HolidaySearchResult searchResult = data().getHolidayMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("holidays/jsonmetadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{holidayId}")
  public WebHolidayResource findHoliday(@PathParam("holidayId") String idStr) {
    data().setUriHolidayId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      HolidayDocument doc = data().getHolidayMaster().get(oid);
      data().setHoliday(doc);
    } catch (DataNotFoundException ex) {
      HolidayHistoryRequest historyRequest = new HolidayHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      HolidayHistoryResult historyResult = data().getHolidayMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setHoliday(historyResult.getFirstDocument());
    }
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
    HolidayMetaDataResult metaData = data().getHolidayMaster().metaData(new HolidayMetaDataRequest());
    out.put("holidayTypes", metaData.getHolidayTypes());
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
  public static URI uri(WebHolidayData data, HolidayType type, ExternalIdBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebHolidaysResource.class);
    if (type != null && identifiers != null) {
      builder.queryParam("type", type.name());
      Iterator<ExternalId> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        ExternalId id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
