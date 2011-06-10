/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RemoteVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteVolatilityCubeDefinitionSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public VolatilityCubeDefinition getDefinition(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    final RestTarget target = getTargetBase().resolveBase(currency.getCode()).resolve(name);
    return getRestClient().getSingleValue(VolatilityCubeDefinition.class, target, "definition");
  }

  @Override
  public VolatilityCubeDefinition getDefinition(Currency currency, String name, InstantProvider version) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(version, "version");
    final RestTarget target = getTargetBase().resolveBase(currency.getCode()).resolveBase(name).resolve(Long.toString(version.toInstant().toEpochMillisLong()));
    return getRestClient().getSingleValue(VolatilityCubeDefinition.class, target, "definition");
  }
}
