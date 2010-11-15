/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
/*package*/ class SecurityResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityResolver.class);
  
  /**
   * A small job that can be run in an executor to resolve a security against
   * a {@link SecuritySource}.
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
      Security security = null;
      try {
        security = _securitySource.getSecurity(_securityKey);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Exception while resolving SecurityKey " + _securityKey, e);
      }
      if (security == null) {
        throw new OpenGammaRuntimeException("Unable to resolve security key " + _securityKey);
      } else {
        _resultMap.put(_securityKey, security);
      }
    }
  }
  
  public static Map<IdentifierBundle, Security> resolveSecurities(Set<IdentifierBundle> securityKeys, ViewCompilationContext viewCompilationContext) {
    ExecutorCompletionService<IdentifierBundle> completionService = new ExecutorCompletionService<IdentifierBundle>(viewCompilationContext.getServices().getExecutorService());
    boolean failed = false;
    
    Map<IdentifierBundle, Security> securitiesByKey = new ConcurrentHashMap<IdentifierBundle, Security>();
    for (IdentifierBundle secKey : securityKeys) {
      if (secKey == null) {
        failed = true;
        s_logger.warn("Had null security key in at least one position");
      } else {
        completionService.submit(new SecurityResolutionJob(viewCompilationContext.getServices().getSecuritySource(), secKey, securitiesByKey), secKey);
      }
    }
    
    for (int i = 0; i < securityKeys.size(); i++) {
      Future<IdentifierBundle> future = null;
      try {
        future = completionService.take();
      } catch (InterruptedException e1) {
        Thread.interrupted();
        s_logger.warn("Interrupted, so didn't finish resolution.");
        failed = true;
        break;
      }
      try {
        future.get();
      } catch (Exception e) {
        s_logger.warn("Got exception resolving securities", e);
        failed = true;
      }
    }
    
    if (failed) {
      throw new OpenGammaRuntimeException("Unable to resolve all securities");
    }
    
    return securitiesByKey;
  }
  
}
