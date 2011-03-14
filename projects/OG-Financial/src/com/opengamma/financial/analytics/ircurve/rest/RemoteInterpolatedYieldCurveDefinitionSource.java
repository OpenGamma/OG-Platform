/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RemoteInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteInterpolatedYieldCurveDefinitionSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
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
  public YieldCurveDefinition getDefinition(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    final RestTarget target = getTargetBase().resolveBase(currency.getCode()).resolve(name);
    return getRestClient().getSingleValue(YieldCurveDefinition.class, target, "definition");
  }

  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, InstantProvider version) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(version, "version");
    final RestTarget target = getTargetBase().resolveBase(currency.getCode()).resolveBase(name).resolve(Long.toString(version.toInstant().toEpochMillisLong()));
    return getRestClient().getSingleValue(YieldCurveDefinition.class, target, "definition");
  }

}
