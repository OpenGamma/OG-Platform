/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.SecurityLinkResolver;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.position.WebPositionsData;
import com.opengamma.web.position.WebPositionsUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;

/**
 * Abstract base class for RESTful portfolio resources.
 */
public abstract class AbstractWebPortfolioResource extends AbstractPerRequestWebResource {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebPortfolioResource.class);
  
  /**
   * The backing bean.
   */
  private final WebPortfoliosData _data;
  /**
   * The security link resolver.
   */
  private SecurityLinkResolver _securityLinkResolver;

  /**
   * Creates the resource.
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param executor  the executor service, not null
   */
  protected AbstractWebPortfolioResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final SecuritySource securitySource, final ExecutorService executor) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(executor, "executor");
    _data = new WebPortfoliosData();
    data().setPortfolioMaster(portfolioMaster);
    data().setPositionMaster(positionMaster);
    _securityLinkResolver = new SecurityLinkResolver(executor, securitySource, VersionCorrection.LATEST);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebPortfolioResource(final AbstractWebPortfolioResource parent) {
    super(parent);
    _data = parent._data;
    _securityLinkResolver = parent._securityLinkResolver;
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
    out.put("uris", new WebPortfoliosUris(data()));
    WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new WebSecuritiesUris(secData));
    WebPositionsData posData = new WebPositionsData(data().getUriInfo());
    out.put("positionUris", new WebPositionsUris(posData));
    return out;
  }

  //-------------------------------------------------------------------------
  protected void resolveSecurities(Collection<ManageablePosition> positions) {
    Collection<SecurityLink> securityLinks = new ArrayList<SecurityLink>(positions.size());
    for (ManageablePosition position : positions) {
      securityLinks.add(position.getSecurityLink());
    }
    if (!securityLinks.isEmpty()) {
      try {
        _securityLinkResolver.resolveSecurities(securityLinks);
      } catch (OpenGammaRuntimeException ex) {
        s_logger.warn("Problem resolving securities in a position", ex);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebPortfoliosData data() {
    return _data;
  }

}
