/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all positions.
 * <p>
 * The positions resource represents the whole of a position master.
 */
@Path("/positions")
public class WebPositionsResource extends AbstractWebPositionResource {
  
  private static final String TYPE = "Positions";
  private static final List<String> DATA_FIELDS = Lists.newArrayList("id", "name", "quantity", "trades");
  
  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   */
  public WebPositionsResource(final PositionMaster positionMaster, final SecurityLoader securityLoader) {
    super(positionMaster, securityLoader);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("minquantity") String minQuantityStr,
      @QueryParam("maxquantity") String maxQuantityStr) {
    FlexiBean out = createSearchResultData(page, pageSize, minQuantityStr, maxQuantityStr);
    return getFreemarker().build("positions/positions.ftl", out);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("minquantity") String minQuantityStr,
      @QueryParam("maxquantity") String maxQuantityStr) {
    FlexiBean out = createSearchResultData(page, pageSize, minQuantityStr, maxQuantityStr);
    PositionSearchResult positionSearchResult = (PositionSearchResult) out.get("searchResult");
    
    return getJSONOutputter().buildJSONSearchResult(TYPE, DATA_FIELDS, formatOutput(positionSearchResult));
  }

  private List<String> formatOutput(PositionSearchResult positionSearchResult) {
    List<String> result = new ArrayList<String>();
    for (PositionDocument item : positionSearchResult.getDocuments()) {
      String id = item.getPosition().getUniqueId().getValue();
      String name = item.getPosition().getName();
      BigDecimal quantity = item.getPosition().getQuantity();
      int tradeSize = item.getPosition().getTrades().size();
      StringBuilder buf = new StringBuilder();
      buf.append(id).append(DELIMITER);
      buf.append(name).append(DELIMITER);
      buf.append(quantity.toString()).append(DELIMITER);
      buf.append(String.valueOf(tradeSize));
      result.add(buf.toString());
    }
    return result;
  }

  private FlexiBean createSearchResultData(int page, int pageSize, String minQuantityStr, String maxQuantityStr) {
    minQuantityStr = StringUtils.defaultString(minQuantityStr).replace(",", "");
    maxQuantityStr = StringUtils.defaultString(maxQuantityStr).replace(",", "");
    FlexiBean out = createRootData();
    
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    if (NumberUtils.isNumber(minQuantityStr)) {
      searchRequest.setMinQuantity(NumberUtils.createBigDecimal(minQuantityStr));
    }
    if (NumberUtils.isNumber(maxQuantityStr)) {
      searchRequest.setMaxQuantity(NumberUtils.createBigDecimal(maxQuantityStr));
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
  public Response post(
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
    IdentifierBundle id = IdentifierBundle.of(Identifier.of(idScheme, idValue));
    Map<IdentifierBundle, UniqueIdentifier> loaded = data().getSecurityLoader().loadSecurity(Collections.singleton(id));
    UniqueIdentifier secUid = loaded.get(id);
    if (secUid == null) {
      FlexiBean out = createRootData();
      out.put("err_idvalueNotFound", true);
      String html = getFreemarker().build("positions/positions-add.ftl", out);
      return Response.ok(html).build();
    }
    SecurityDocument secDoc = data().getSecurityLoader().getSecurityMaster().get(secUid);
    ManageablePosition position = new ManageablePosition(quantity, secDoc.getSecurity().getIdentifiers());
    PositionDocument doc = new PositionDocument(position);
    doc = data().getPositionMaster().add(doc);
    data().setPosition(doc);
    URI uri = WebPositionResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public WebPositionResource findPosition(@PathParam("positionId") String idStr) {
    data().setUriPositionId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
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
