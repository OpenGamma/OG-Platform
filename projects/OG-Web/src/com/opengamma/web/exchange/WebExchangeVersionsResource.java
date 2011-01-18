/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;

/**
 * RESTful resource for all versions of an exchange.
 */
@Path("/exchanges/{exchangeId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebExchangeVersionsResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebExchangeVersionsResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(data().getExchange().getUniqueId());
    ExchangeHistoryResult result = data().getExchangeMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getExchanges());
    return getFreemarker().build("exchanges/exchangeversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ExchangeDocument doc = data().getExchange();
    out.put("exchangeDoc", doc);
    out.put("exchange", doc.getExchange());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebExchangeVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    ExchangeDocument doc = data().getExchange();
    UniqueIdentifier combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      ExchangeDocument versioned = data().getExchangeMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebExchangeVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    String exchangeId = data.getBestExchangeUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeVersionsResource.class).build(exchangeId);
  }

}
