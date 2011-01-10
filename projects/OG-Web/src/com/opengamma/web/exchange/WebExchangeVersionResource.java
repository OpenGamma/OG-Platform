/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;

/**
 * RESTful resource for a version of a exchange.
 */
@Path("/exchanges/{exchangeId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebExchangeVersionResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebExchangeVersionResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("exchanges/exchangeversion.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ExchangeDocument latestDoc = data().getExchange();
    ExchangeDocument versionedExchange = data().getVersioned();
    out.put("latestExchangeDoc", latestDoc);
    out.put("latestExchange", latestDoc.getExchange());
    out.put("exchangeDoc", versionedExchange);
    out.put("exchange", versionedExchange.getExchange());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data, final UniqueIdentifier overrideVersionId) {
    String exchangeId = data.getBestExchangeUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeVersionResource.class).build(exchangeId, versionId);
  }

}
