/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteCurrencyMatrixSource implements CurrencyMatrixSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteCurrencyMatrixSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
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
  public CurrencyMatrix getCurrencyMatrix(String name) {
    ArgumentChecker.notNull(name, "name");
    return getRestClient().getSingleValue(CurrencyMatrix.class, getTargetBase().resolve(name), "matrix");
  }

}
