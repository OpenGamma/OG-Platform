/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

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
import com.opengamma.web.position.WebPositionsData;
import com.opengamma.web.position.WebPositionsUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;

/**
 * Abstract base class for RESTful portfolio resources.
 */
public abstract class AbstractWebPortfolioResource
    extends AbstractPerRequestWebResource<WebPortfoliosData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "portfolios/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "portfolios/json/";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebPortfolioResource.class);

  /**
   * The security link resolver.
   */
  private SecurityLinkResolver _securityLinkResolver;

  /**
   * Creates the resource.
   * 
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param executor  the executor service, not null
   */
  protected AbstractWebPortfolioResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final SecuritySource securitySource, final ExecutorService executor) {
    super(new WebPortfoliosData());
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(executor, "executor");
    data().setPortfolioMaster(portfolioMaster);
    data().setPositionMaster(positionMaster);
    _securityLinkResolver = new SecurityLinkResolver(executor, securitySource, VersionCorrection.LATEST);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebPortfolioResource(final AbstractWebPortfolioResource parent) {
    super(parent);
    _securityLinkResolver = parent._securityLinkResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
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

}
