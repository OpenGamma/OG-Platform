/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ExchangeHistoryRequest;
import com.opengamma.financial.world.exchange.master.ExchangeHistoryResult;
import com.opengamma.id.UniqueIdentifier;

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
    ExchangeHistoryRequest request = new ExchangeHistoryRequest();
    request.setExchangeId(data().getExchange().getExchangeId());
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
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebExchangeVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    ExchangeDocument doc = data().getExchange();
    UniqueIdentifier combined = doc.getExchangeId().withVersion(idStr);
    if (doc.getExchangeId().equals(combined) == false) {
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
