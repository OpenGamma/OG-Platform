/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.Maps;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;

/**
 * RESTful resource for a security.
 */
@Path("/securities/{securityId}")
public class WebSecurityResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebSecurityResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/security.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/jsonsecurity.ftl", out);
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    URI uri = updateSecurity(doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {  // TODO: idempotent
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    updateSecurity(doc);
    return Response.ok().build();
  }

  private URI updateSecurity(SecurityDocument doc) {
    IdentifierBundle identifierBundle = doc.getSecurity().getIdentifiers();
    data().getSecurityLoader().loadSecurity(Collections.singleton(identifierBundle));
    return WebSecurityResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getSecurityMaster().remove(doc.getUniqueId());
    URI uri = WebSecurityResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest()) {  // idempotent DELETE
      data().getSecurityMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    SecurityDocument doc = data().getSecurity();
    out.put("securityDoc", doc);
    out.put("security", doc.getSecurity());
    out.put("deleted", !doc.isLatest());
    addSecuritySpecificMetaData(doc.getSecurity(), out);
    return out;
  }

  private void addSecuritySpecificMetaData(ManageableSecurity security, FlexiBean out) {
    if (security.getSecurityType().equals("SWAP")) {
      SwapSecurity swapSecurity = (SwapSecurity) security;
      out.put("payLegType", swapSecurity.getPayLeg().accept(new SwapLegClassifierVisitor()));
      out.put("receiveLegType", swapSecurity.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    }
    if (security.getSecurityType().equals(FutureSecurity.SECURITY_TYPE)) {
      FutureSecurity futureSecurity = (FutureSecurity) security;
      out.put("futureSecurityType", futureSecurity.accept(new FutureSecurityTypeVisitor()));
      out.put("basket", getBondFutureBasket(security));
    }
  }
  
  private Map<String, Double> getBondFutureBasket(ManageableSecurity security) {
    Map<String, Double> result = Maps.newHashMap();
    if (security instanceof BondFutureSecurity) {
      BondFutureSecurity bondFutureSecurity = (BondFutureSecurity) security;
      List<BondFutureDeliverable> basket = bondFutureSecurity.getBasket();
      for (BondFutureDeliverable bondFutureDeliverable : basket) {
        String identifierValue = bondFutureDeliverable.getIdentifiers().getIdentifierValue(SecurityUtils.BLOOMBERG_BUID);
        result.put("BLOOMBERG BUID - " + identifierValue, bondFutureDeliverable.getConversionFactor());
      }
    }
    return result;
  }

  private class FutureSecurityTypeVisitor implements FutureSecurityVisitor<String> {

    @Override
    public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
      return "AgricultureFuture";
    }

    @Override
    public String visitBondFutureSecurity(BondFutureSecurity security) {
      return "BondFuture";
    }

    @Override
    public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
      return "EnergyFuture";
    }

    @Override
    public String visitEquityFutureSecurity(EquityFutureSecurity security) {
      return "EquityFuture";
    }

    @Override
    public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
      return "EquityIndexDividendFuture";
    }

    @Override
    public String visitFXFutureSecurity(FXFutureSecurity security) {
      return "FxFuture";
    }

    @Override
    public String visitIndexFutureSecurity(IndexFutureSecurity security) {
      return "IndexFuture";
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
      return "InterestRate";
    }

    @Override
    public String visitMetalFutureSecurity(MetalFutureSecurity security) {
      return "MetalFuture";
    }

    @Override
    public String visitStockFutureSecurity(StockFutureSecurity security) {
      return "StockFuture";
    }

  }

  private class SwapLegClassifierVisitor implements SwapLegVisitor<String> {
    @Override
    public String visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return "FixedInterestRateLeg";
    }

    @Override
    public String visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return "FloatingInterestRateLeg";
    }
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebSecurityVersionsResource findVersions() {
    return new WebSecurityVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideSecurityId  the override security id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data, final UniqueIdentifier overrideSecurityId) {
    String securityId = data.getBestSecurityUriId(overrideSecurityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityResource.class).build(securityId);
  }

}
