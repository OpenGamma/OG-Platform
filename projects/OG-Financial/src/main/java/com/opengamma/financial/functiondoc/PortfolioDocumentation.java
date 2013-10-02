/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.functiondoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.PortfolioSearchIterator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MdcAwareThreadPoolExecutor;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Utility template for generating documentation from a function repository by iterating through
 * the defined portfolios.
 */
public class PortfolioDocumentation extends AbstractDocumentation {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioDocumentation.class);

  private final PortfolioMaster _portfolioMaster;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ExecutorService _executorService;

  public PortfolioDocumentation(final CompiledFunctionRepository functionRepository, final FunctionExclusionGroups functionExclusionGroups, final PortfolioMaster portfolioMaster,
      final PositionSource positionSource, final SecuritySource securitySource) {
    super(functionRepository, functionExclusionGroups);
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(portfolioMaster, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _portfolioMaster = portfolioMaster;
    _positionSource = positionSource;
    _securitySource = securitySource;
    final int threads = 32;
    final ThreadPoolExecutor executor = new MdcAwareThreadPoolExecutor(threads, threads, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadPoolFactory("doc"));
    executor.allowCoreThreadTimeOut(true);
    _executorService = executor;
  }

  protected PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  protected PositionSource getPositionSource() {
    return _positionSource;
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected Collection<UniqueId> getPortfolios() {
    s_logger.debug("Querying portfolios available");
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(0);
    request.setIncludePositions(false);
    request.setName(null);
    final Collection<UniqueId> result = new ArrayList<UniqueId>();
    for (PortfolioDocument document : PortfolioSearchIterator.iterable(_portfolioMaster, request)) {
      result.add(document.getUniqueId());
    }
    s_logger.info("Found {} portfolios", result.size());
    return result;
  }

  @Override
  public void run() {
    int count = Integer.MIN_VALUE; // set to e.g. -10 to just consider the first 10 portfolios found
    for (final UniqueId portfolioId : getPortfolios()) {
      try {
        final long t1 = System.nanoTime();
        final Portfolio rawPortfolio = getPositionSource().getPortfolio(portfolioId, VersionCorrection.LATEST);
        final long t2 = System.nanoTime();
        final Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(rawPortfolio, getExecutorService(), getSecuritySource());
        final long t3 = System.nanoTime();
        s_logger.debug("Got portfolio {} in {}ms", portfolioId, (double) (t2 - t1) / 1e6);
        s_logger.debug("Resolved portfolio {} in {}ms", portfolioId, (double) (t3 - t2) / 1e6);
        getExecutorService().execute(new Runnable() {
          @Override
          public void run() {
            processAvailablePortfolioOutputs(resolvedPortfolio);
          }
        });
        if ((++count) > 0) {
          break;
        }
      } catch (OpenGammaRuntimeException e) {
        s_logger.debug("Couldn't resolve {} - {}", portfolioId, e);
      }
    }
    try {
      s_logger.info("Waiting for portfolio analysis");
      getExecutorService().shutdown();
      getExecutorService().awaitTermination(30, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    super.run();
  }

}
