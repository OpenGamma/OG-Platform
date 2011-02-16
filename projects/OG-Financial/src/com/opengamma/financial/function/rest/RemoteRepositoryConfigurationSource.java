/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to a repository configuration source.
 */
public class RemoteRepositoryConfigurationSource implements RepositoryConfigurationSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteRepositoryConfigurationSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
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
  public RepositoryConfiguration getRepositoryConfiguration() {
    return getRestClient().getSingleValue(RepositoryConfiguration.class, getTargetBase().resolve("repositoryConfiguration"), "repositoryConfiguration");
  }

}
