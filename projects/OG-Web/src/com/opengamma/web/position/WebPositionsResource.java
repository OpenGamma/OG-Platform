/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all positions.
 * <p>
 * The positions resource represents the whole of a position master.
 */
@Path("/positions")
public class WebPositionsResource extends AbstractWebPositionResource {
  
  private static final TimeZone DEFAULT_TIMEZONE = Clock.systemDefaultZone().getZone();

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   * @param htsMaster       the HTS master, not null (for resolving relevant HTS Id)
   * @param cfgSource       the config master, not null (for resolving relevant HTS Id)
   */
  public WebPositionsResource(final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesMaster htsMaster, final ConfigSource cfgSource) {
    super(positionMaster, securityLoader, securitySource, htsMaster, cfgSource);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("identifier") String identifier,
      @QueryParam("minquantity") String minQuantityStr,
      @QueryParam("maxquantity") String maxQuantityStr,
      @QueryParam("positionId") List<String> positionIdStrs,
      @QueryParam("tradeId") List<String> tradeIdStrs) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, identifier, minQuantityStr, maxQuantityStr, positionIdStrs, tradeIdStrs);
    return getFreemarker().build("positions/positions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("identifier") String identifier,
      @QueryParam("minquantity") String minQuantityStr,
      @QueryParam("maxquantity") String maxQuantityStr,
      @QueryParam("positionId") List<String> positionIdStrs,
      @QueryParam("tradeId") List<String> tradeIdStrs) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, identifier, minQuantityStr, maxQuantityStr, positionIdStrs, tradeIdStrs);
    return getFreemarker().build("positions/jsonpositions.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, String identifier, String minQuantityStr,
      String maxQuantityStr, List<String> positionIdStrs, List<String> tradeIdStrs) {
    minQuantityStr = StringUtils.defaultString(minQuantityStr).replace(",", "");
    maxQuantityStr = StringUtils.defaultString(maxQuantityStr).replace(",", "");
    FlexiBean out = createRootData();
    
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSecurityIdValue(StringUtils.trimToNull(identifier));
    if (NumberUtils.isNumber(minQuantityStr)) {
      searchRequest.setMinQuantity(NumberUtils.createBigDecimal(minQuantityStr));
    }
    if (NumberUtils.isNumber(maxQuantityStr)) {
      searchRequest.setMaxQuantity(NumberUtils.createBigDecimal(maxQuantityStr));
    }
    for (String positionIdStr : positionIdStrs) {
      searchRequest.addPositionObjectId(ObjectId.parse(positionIdStr));
    }
    for (String tradeIdStr : tradeIdStrs) {
      searchRequest.addPositionObjectId(ObjectId.parse(tradeIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      PositionSearchResult searchResult = data().getPositionMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("quantity") String quantityStr,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    quantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
    BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    if (quantity == null || idScheme == null || idValue == null) {
      FlexiBean out = createRootData();
      if (quantityStr == null) {
        out.put("err_quantityMissing", true);
      }
      if (quantity == null) {
        out.put("err_quantityNotNumeric", true);
      }
      if (idScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      }
      String html = getFreemarker().build("positions/positions-add.ftl", out);
      return Response.ok(html).build();
    }
    ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(idScheme, idValue));
    UniqueId secUid = getSecurityUniqueId(id);
    if (secUid == null) {
      FlexiBean out = createRootData();
      out.put("err_idvalueNotFound", true);
      String html = getFreemarker().build("positions/positions-add.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = addPosition(quantity, secUid);
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("quantity") String quantityStr,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("tradesJson") String tradesJson) {
    
    quantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
    BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    tradesJson = StringUtils.trimToNull(tradesJson);
    
    if (quantity == null || idScheme == null || idValue == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    
    ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(idScheme, idValue));
    UniqueId secUid = getSecurityUniqueId(id);
    if (secUid == null) {
      throw new DataNotFoundException("invalid " + idScheme + "~" + idValue);
    }
    Collection<ManageableTrade> trades = null;
    if (tradesJson != null) {
      trades = parseTrades(tradesJson);
    } else {
      trades = Collections.<ManageableTrade>emptyList();
    }
    URI uri = addPosition(quantity, secUid, trades);
    return Response.created(uri).build();
  }

  private UniqueId getSecurityUniqueId(ExternalIdBundle id) {
    UniqueId result = null;
    Security security = data().getSecuritySource().getSecurity(id);
    if (security != null) {
      result = security.getUniqueId();
    } else {
      Map<ExternalIdBundle, UniqueId> loaded = data().getSecurityLoader().loadSecurity(Collections.singleton(id));
      result = loaded.get(id);
    }
    return result;
  }

  private Set<ManageableTrade> parseTrades(String tradesJson) {
    Set<ManageableTrade> trades = Sets.newHashSet();
    try {
      JSONObject jsonObject = new JSONObject(tradesJson);
      if (jsonObject.has("trades")) {
        JSONArray jsonArray = jsonObject.getJSONArray("trades");
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject tradeJson = jsonArray.getJSONObject(i);
          ManageableTrade trade = new ManageableTrade();
          TimeZone timeZone = null;
          if (tradeJson.has("timeZone")) {
            String timeZoneId = StringUtils.trimToNull(tradeJson.getString("timeZone"));
            if (timeZoneId != null) {
              timeZone = TimeZone.of(timeZoneId);
            } else {
              timeZone = DEFAULT_TIMEZONE;
            }
          }
          if (tradeJson.has("premium")) {
            trade.setPremium(tradeJson.getDouble("premium"));
          }
          if (tradeJson.has("counterParty")) {
            trade.setCounterpartyExternalId(ExternalId.of(Counterparty.DEFAULT_SCHEME, tradeJson.getString("counterParty")));
          }
          if (tradeJson.has("premiumCurrency")) {
            trade.setPremiumCurrency(Currency.of(tradeJson.getString("premiumCurrency")));
          }
          if (tradeJson.has("premiumDate")) {
            LocalDate premiumDate = LocalDate.parse(tradeJson.getString("premiumDate"));
            trade.setPremiumDate(premiumDate);
            if (tradeJson.has("premiumTime")) {
              LocalTime premiumTime = LocalTime.parse(tradeJson.getString("premiumTime"));
              ZonedDateTime zonedDateTime = ZonedDateTime.of(premiumDate, premiumTime, timeZone);
              trade.setPremiumTime(zonedDateTime.toOffsetTime());
            }
          }
          if (tradeJson.has("quantity")) {
            trade.setQuantity(new BigDecimal(tradeJson.getString("quantity")));
          }
          if (tradeJson.has("tradeDate")) {
            LocalDate tradeDate = LocalDate.parse(tradeJson.getString("tradeDate"));
            trade.setTradeDate(tradeDate);
            if (tradeJson.has("tradeTime")) {
              LocalTime tradeTime = LocalTime.parse(tradeJson.getString("tradeTime"));
              ZonedDateTime zonedDateTime = ZonedDateTime.of(tradeDate, tradeTime, timeZone);
              trade.setTradeTime(zonedDateTime.toOffsetTime());
            }    
          }
          trades.add(trade);
        }
      } else {
        throw new OpenGammaRuntimeException("missing trades field in trades json document");
      }
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Error parsing Json document for Trades", ex);
    }
    return trades;
  }

  private URI addPosition(BigDecimal quantity, UniqueId secUid) {
    return addPosition(quantity, secUid, Collections.<ManageableTrade>emptyList());
  }
  
  private URI addPosition(BigDecimal quantity, UniqueId secUid, Collection<ManageableTrade> trades) {
    SecurityDocument secDoc = data().getSecurityLoader().getSecurityMaster().get(secUid);
    ExternalIdBundle secId = secDoc.getSecurity().getExternalIdBundle();
    ManageablePosition position = new ManageablePosition(quantity, secId);
    for (ManageableTrade trade : trades) {
      trade.setSecurityLink(new ManageableSecurityLink(secId));
      position.addTrade(trade);
    }
    PositionDocument doc = new PositionDocument(position);
    doc = data().getPositionMaster().add(doc);
    data().setPosition(doc);
    return WebPositionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public WebPositionResource findPosition(@PathParam("positionId") String idStr) {
    data().setUriPositionId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      PositionDocument doc = data().getPositionMaster().get(oid);
      data().setPosition(doc);
    } catch (DataNotFoundException ex) {
      PositionHistoryRequest historyRequest = new PositionHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      PositionHistoryResult historyResult = data().getPositionMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setPosition(historyResult.getFirstDocument());
    }
    return new WebPositionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for positions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebPositionsData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionsResource.class).build();
  }

}
