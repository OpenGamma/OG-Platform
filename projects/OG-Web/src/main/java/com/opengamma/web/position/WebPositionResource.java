/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import static org.apache.commons.lang.StringEscapeUtils.escapeJavaScript;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.replace;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.base.Objects;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.JodaBeanSerialization;

/**
 * RESTful resource for a position.
 */
@Path("/positions/{positionId}")
public class WebPositionResource extends AbstractWebPositionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPositionResource(final AbstractWebPositionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "position.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "position.ftl", out);
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(@FormParam(POSITION_XML) String positionXml) {
    PositionDocument doc = data().getPosition();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    positionXml = trimToNull(positionXml);
    if (positionXml == null) {
      FlexiBean out = createRootData();
      out.put("err_xmlMissing", true);
      out.put(POSITION_XML, defaultString(positionXml));
      String html = getFreemarker().build(HTML_DIR + "position-update.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = updatePosition(positionXml);
    return Response.seeOther(uri).build();
  }

  private URI updatePosition(String positionXml) {
    Bean positionBean = JodaBeanSerialization.deserializer().xmlReader().read(positionXml);
    PositionMaster positionMaster = data().getPositionMaster();
    positionMaster.update(new PositionDocument((ManageablePosition) positionBean));
    return WebPositionResource.uri(data());
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(@FormParam("quantity") String quantityStr, 
      @FormParam("tradesJson") String tradesJson, 
      @FormParam("type") String type, 
      @FormParam(POSITION_XML) String positionXml) {
    
    PositionDocument doc = data().getPosition();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    type = defaultString(trimToNull(type));
    switch (type) {
      case "xml":
        updatePosition(trimToEmpty(positionXml));
        break;
      default:
        quantityStr = replace(trimToNull(quantityStr), ",", "");
        tradesJson = trimToNull(tradesJson);
        Collection<ManageableTrade> trades = null;
        if (tradesJson != null) {
          trades = parseTrades(tradesJson);
        } else {
          trades = Collections.<ManageableTrade>emptyList();
        }
        BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
        updatePosition(doc, quantity, trades);
    }
    return Response.ok().build();
  }

  private URI updatePosition(PositionDocument doc, BigDecimal quantity, Collection<ManageableTrade> trades) {
    ManageablePosition position = doc.getPosition();
    if (Objects.equal(position.getQuantity(), quantity) == false || trades != null) {
      position.setQuantity(quantity);
      position.getTrades().clear();
      for (ManageableTrade trade : trades) {
        trade.setSecurityLink(position.getSecurityLink());
        position.addTrade(trade);
      }
      doc = data().getPositionMaster().update(doc);
      data().setPosition(doc);
    }
    return WebPositionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    PositionDocument doc = data().getPosition();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    URI uri = deletePosition(doc);
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    PositionDocument doc = data().getPosition();
    if (doc.isLatest()) {
      deletePosition(doc);
    }
    return Response.ok().build();
  }

  private URI deletePosition(PositionDocument doc) {
    data().getPositionMaster().remove(doc.getUniqueId());
    return WebPositionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("timeFormatterJson", DateTimeFormatter.ofPattern("HH:mm:ss"));
    out.put("offsetFormatterJson", DateTimeFormatter.ofPattern("Z"));
    PositionDocument doc = data().getPosition();
    
    // REVIEW jonathan 2012-01-12 -- we are throwing away any adjuster that may be required, e.g. to apply
    // normalisation to the time-series. This reproduces the previous behaviour but probably indicates that the
    // time-series information is in the wrong place.
    
    ObjectId tsObjectId = null;
    Security security = doc.getPosition().getSecurityLink().resolveQuiet(data().getSecuritySource());
    if (security != null && !security.getExternalIdBundle().isEmpty()) {
      // Get the last price HTS for the security
      HistoricalTimeSeriesSource htsSource = data().getHistoricalTimeSeriesSource();
      HistoricalTimeSeries series = htsSource.getHistoricalTimeSeries(
          MarketDataRequirementNames.MARKET_VALUE, doc.getPosition().getSecurity().getExternalIdBundle(), null, null, false, null, false, 0);
      if (series != null) {
        tsObjectId = series.getUniqueId().getObjectId();
      }
    }
    
    out.put("positionDoc", doc);
    out.put("position", doc.getPosition());
    out.put("security", doc.getPosition().getSecurity());
    out.put("timeSeriesId", tsObjectId);
    out.put("deleted", !doc.isLatest());
    out.put("attributes", doc.getPosition().getAttributes());
    out.put("tradeAttrModel", getTradeAttributesModel());
    out.put(POSITION_XML, escapeJavaScript(defaultString(getPositionXml(doc.getPosition()))));
    return out;
  }

  private TradeAttributesModel getTradeAttributesModel() {
    PositionDocument doc = data().getPosition();
    TradeAttributesModel getTradeAttributesModel = new TradeAttributesModel(doc.getPosition());
    return getTradeAttributesModel;
  }
  
  //-------------------------------------------------------------------------
  @Path("versions")
  public WebPositionVersionsResource findVersions() {
    return new WebPositionVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overridePositionId  the override position id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data, final UniqueId overridePositionId) {
    String positionId = data.getBestPositionUriId(overridePositionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionResource.class).build(positionId);
  }

}
