/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility to resolve securities in bulk.
 */
public final class SecurityResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityResolver.class);

  /**
   * Restricted constructor.
   */
  private SecurityResolver() {
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves securities in bulk.
   * 
   * @param securityKeys  the bundles to lookup, not null
   * @param executorService  the threading service, not null
   * @param securitySource  the security source, not null
   * @return the map of resolved securities, not null
   */
  public static Map<IdentifierBundle, Security> resolveSecurities(final Set<IdentifierBundle> securityKeys, final ExecutorService executorService, final SecuritySource securitySource) {
    ArgumentChecker.noNulls(securityKeys, "securityKeys");
    final ExecutorCompletionService<IdentifierBundle> completionService = new ExecutorCompletionService<IdentifierBundle>(executorService);
    boolean failed = false;
    
    final Map<IdentifierBundle, Security> securitiesByKey = new ConcurrentHashMap<IdentifierBundle, Security>();
    final List<Future<IdentifierBundle>> submitted = Lists.newArrayListWithCapacity(securityKeys.size());
    for (IdentifierBundle secKey : securityKeys) {
      SecurityResolutionJob task = new SecurityResolutionJob(securitySource, secKey, securitiesByKey);
      submitted.add(completionService.submit(task, secKey));
    }
    
    for (int i = 0; i < submitted.size(); i++) {
      final Future<IdentifierBundle> future;
      try {
        future = completionService.take();
      } catch (InterruptedException ex) {
        Thread.interrupted();
        s_logger.warn("Interrupted, so didn't finish resolution");
        failed = true;
        break;
      }
      try {
        future.get();
      } catch (Exception ex) {
        s_logger.warn("Got exception resolving securities", ex);
        failed = true;
        break;
      }
    }
    if (failed) {
      for (Future<IdentifierBundle> future : submitted) {
        future.cancel(false);
      }
      throw new OpenGammaRuntimeException("Unable to resolve all securities");
    }
    return securitiesByKey;
  }

  /**
   * Resolves securities in bulk.
   * 
   * @param securityKeys  the bundles to lookup, not null
   * @param viewCompilationContext  the context, not null
   * @return the map of resolved securities, not null
   */
  public static Map<IdentifierBundle, Security> resolveSecurities(Set<IdentifierBundle> securityKeys, ViewCompilationContext viewCompilationContext) {
    return resolveSecurities(securityKeys, viewCompilationContext.getServices().getExecutorService(), viewCompilationContext.getServices().getSecuritySource());
  }

  //-------------------------------------------------------------------------
  /**
   * A small job that can be run in an executor to resolve a security against a security source.
   */
  private static class SecurityResolutionJob implements Runnable {
    private final SecuritySource _securitySource;
    private final IdentifierBundle _securityKey;
    private final Map<IdentifierBundle, Security> _resultMap;

    public SecurityResolutionJob(SecuritySource securitySource, IdentifierBundle securityKey, Map<IdentifierBundle, Security> resultMap) {
      _securitySource = securitySource;
      _securityKey = securityKey;
      _resultMap = resultMap;
    }

    @Override
    public void run() {
      final Security security;
      try {
        security = _securitySource.getSecurity(_securityKey);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Exception while resolving SecurityKey " + _securityKey, ex);
      }
      if (security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + _securityKey);
      }
      _resultMap.put(_securityKey, security);
    }
  }

}
