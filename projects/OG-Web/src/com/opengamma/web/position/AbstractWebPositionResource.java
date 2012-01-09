/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;

/**
 * Abstract base class for RESTful position resources.
 */
public abstract class AbstractWebPositionResource extends AbstractWebResource {
  
  /**
   * The backing bean.
   */
  private final WebPositionsData _data;

  /**
   * The HTS resolver (for getting an HTS Id)
   */
  private final HistoricalTimeSeriesResolver _htsResolver;

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   * @param htsMaster       the historical time series master, not null
   * @param cfgSource       the config source, not null
   */
  protected AbstractWebPositionResource(
      final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesMaster htsMaster, final ConfigSource cfgSource) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _data = new WebPositionsData();
    data().setPositionMaster(positionMaster);
    data().setSecurityLoader(securityLoader);
    data().setSecuritySource(securitySource);
    
    _htsResolver = new DefaultHistoricalTimeSeriesResolver(htsMaster, cfgSource);    
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebPositionResource(final AbstractWebPositionResource parent) {
    super(parent);
    _data = parent._data;
    _htsResolver = parent._htsResolver;
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebPositionsUris(data()));
    WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new WebSecuritiesUris(secData));
    
    return out;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebPositionsData data() {
    return _data;
  }
  
  /**
   * Gets the HTS resolver
   * @return the HTS resolver, not null
   */
  protected HistoricalTimeSeriesResolver htsResolver() {
    return _htsResolver;
  }

  protected Set<ManageableTrade> parseTrades(String tradesJson) {
    Set<ManageableTrade> trades = Sets.newHashSet();
    try {
      JSONObject jsonObject = new JSONObject(tradesJson);
      if (jsonObject.has("trades")) {
        JSONArray jsonArray = jsonObject.getJSONArray("trades");
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject tradeJson = jsonArray.getJSONObject(i);
          ManageableTrade trade = new ManageableTrade();
     
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
              ZoneOffset premiumOffset = getOffset(tradeJson, "premiumOffset");
              ZonedDateTime zonedDateTime = ZonedDateTime.of(premiumDate, premiumTime, premiumOffset.toTimeZone());
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
              ZoneOffset tradeOffset = getOffset(tradeJson, "tradeOffset");
              ZonedDateTime zonedDateTime = ZonedDateTime.of(tradeDate, tradeTime, tradeOffset.toTimeZone());
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

  private ZoneOffset getOffset(JSONObject tradeJson, String fieldName) throws JSONException {
    ZoneOffset premiumOffset = ZoneOffset.UTC;
    if (tradeJson.has(fieldName)) {
      String offsetId = StringUtils.trimToNull(tradeJson.getString(fieldName));
      if (offsetId != null) {
        premiumOffset = ZoneOffset.of(offsetId);
      } 
    }
    return premiumOffset;
  }
  
}
