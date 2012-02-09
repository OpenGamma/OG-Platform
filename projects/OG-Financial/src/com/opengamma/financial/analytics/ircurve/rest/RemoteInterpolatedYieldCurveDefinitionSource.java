/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.time.InstantProvider;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote {@link InterpolatedYieldCurveDefinitionSource}.
 */
public class RemoteInterpolatedYieldCurveDefinitionSource extends AbstractRemoteClient implements InterpolatedYieldCurveDefinitionSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteInterpolatedYieldCurveDefinitionSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    
    URI uri = DataInterpolatedYieldCurveDefinitionSourceResource.uriSearchSingle(getBaseUri(), currency, name, null);
    return accessRemote(uri).get(YieldCurveDefinition.class);
  }

  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, InstantProvider versionAsOf) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    
    URI uri = DataInterpolatedYieldCurveDefinitionSourceResource.uriSearchSingle(getBaseUri(), currency, name, versionAsOf);
    return accessRemote(uri).get(YieldCurveDefinition.class);
  }

}
