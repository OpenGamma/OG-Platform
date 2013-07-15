/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.concurrent.ExecutorService;

import com.google.common.base.Supplier;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Supplies a fully resolved portfolio.
 */
/* package */ class PortfolioSupplier implements Supplier<Portfolio> {

  private final ObjectId _portfolioId;
  private final VersionCorrection _versionCorrection;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ExecutorService _executor;

  /* package */ PortfolioSupplier(ObjectId portfolioId,
                                  VersionCorrection versionCorrection,
                                  PositionSource positionSource,
                                  SecuritySource securitySource,
                                  ExecutorService executor) {
    _securitySource = securitySource;
    _executor = executor;
    ArgumentChecker.notNull(positionSource, "positionSource");
    _positionSource = positionSource;
    _portfolioId = portfolioId;
    _versionCorrection = versionCorrection;
  }

  @Override
  public Portfolio get() {
    if (_portfolioId != null) {
      Portfolio unresolvedPortfolio = _positionSource.getPortfolio(_portfolioId, _versionCorrection);
      return PortfolioCompiler.resolvePortfolio(unresolvedPortfolio, _executor, _securitySource);
    } else {
      return null;
    }
  }
}
