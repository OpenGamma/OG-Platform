/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import org.fudgemsg.FudgeContext;

import redis.clients.jedis.JedisPool;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.RedisCachingSecuritySource;

/**
 * 
 */
public class RedisCachingFinancialSecuritySource extends RedisCachingSecuritySource implements FinancialSecuritySource {
  private final FinancialSecuritySource _financialUnderlying;

  public RedisCachingFinancialSecuritySource(FinancialSecuritySource underlying, JedisPool jedisPool, String redisPrefix, FudgeContext fudgeContext) {
    super(underlying, jedisPool, redisPrefix, fudgeContext);
    _financialUnderlying = underlying;
  }

  /**
   * Gets the financialUnderlying.
   * @return the financialUnderlying
   */
  protected FinancialSecuritySource getFinancialUnderlying() {
    return _financialUnderlying;
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    Collection<Security> results = getFinancialUnderlying().getBondsWithIssuerName(issuerName);
    processResults(results);
    return results;
  }

}
