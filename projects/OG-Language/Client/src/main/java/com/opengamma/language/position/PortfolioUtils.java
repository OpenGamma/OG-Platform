/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.Profiler;

/**
 * Common utilities for working with portfolios.
 */
public final class PortfolioUtils {

  private static final Profiler s_profilerGet = Profiler.create(PortfolioUtils.class, "Get");
  private static final Profiler s_profilerResolve = Profiler.create(PortfolioUtils.class, "Resolve");

  private final GlobalContext _context;

  /**
   * Instantiated version associated with a given context.
   * 
   * @param context the associated context
   */
  public PortfolioUtils(final GlobalContext context) {
    ArgumentChecker.notNull(context, "context");
    _context = context;
  }

  private GlobalContext getContext() {
    return _context;
  }

  public Portfolio getPortfolio(final UniqueId identifier) {
    return getPortfolio(getContext(), identifier);
  }

  public static Portfolio getPortfolio(final GlobalContext context, final UniqueId identifier) {
    final PositionSource positionSource = context.getPositionSource();
    if (positionSource == null) {
      return null;
    }
    s_profilerGet.begin();
    try {
      return positionSource.getPortfolio(identifier, VersionCorrection.LATEST);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (IllegalArgumentException ex) {
      return null;
    } finally {
      s_profilerGet.end();
    }
  }

  public Portfolio getResolvedPortfolio(final UniqueId identifier) {
    return getResolvedPortfolio(getContext(), identifier);
  }

  public static Portfolio getResolvedPortfolio(final GlobalContext context, final UniqueId identifier) {
    final Portfolio portfolio = getPortfolio(context, identifier);
    if (portfolio == null) {
      return null;
    }
    if (context.getSecuritySource() == null) {
      return portfolio;
    }
    s_profilerResolve.begin();
    try {
      return PortfolioCompiler.resolvePortfolio(portfolio, context.getSaturatingExecutor(), context.getSecuritySource());
    } finally {
      s_profilerResolve.end();
    }
  }

  public Portfolio getPortfolio(final UniqueId identifier, final boolean resolveSecurities) {
    return getPortfolio(getContext(), identifier, resolveSecurities);
  }

  public static Portfolio getPortfolio(final GlobalContext context, final UniqueId identifier, final boolean resolveSecurities) {
    if (resolveSecurities) {
      return getResolvedPortfolio(context, identifier);
    } else {
      return getPortfolio(context, identifier);
    }
  }

}
