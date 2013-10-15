/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.UnderlyingSecurityVisitor;

/**
 * Extracts positions, securities and trades from a fully resolved portfolio. Also looks up underlying securities
 * for OTCs with an OTC underlying (e.g. swaptions).
 */
/* package */ class PortfolioEntityExtractor implements PortfolioMapperFunction<List<UniqueIdentifiable>> {

  /** For getting underlying securities for OTCs with OTC underlyings. */
  private final UnderlyingSecurityVisitor _underlyingVisitor;

  /* package */ PortfolioEntityExtractor(VersionCorrection versionCorrection, SecurityMaster securityMaster) {
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _underlyingVisitor = new UnderlyingSecurityVisitor(versionCorrection, securityMaster);
  }

  @Override
  public List<UniqueIdentifiable> apply(PortfolioNode node) {
    return Collections.emptyList();
  }

  @Override
  public List<UniqueIdentifiable> apply(PortfolioNode parent, Position position) {
    List<UniqueIdentifiable> entities = Lists.newArrayList();
    entities.add(position);
    Security security = position.getSecurityLink().getTarget();
    entities.add(security);
    if (security instanceof FinancialSecurity) {
      ManageableSecurity underlying = ((FinancialSecurity) security).accept(_underlyingVisitor);
      if (underlying != null) {
        entities.add(underlying);
      }
    }
    for (Trade trade : position.getTrades()) {
      entities.add(trade);
    }
    return entities;
  }
}
