/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.exchange.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to an {@link ExchangeSource}.
 */
public class RemoteExchangeSource implements ExchangeSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteExchangeSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
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
  public Exchange getExchange(final UniqueId uniqueId) {
    final RestTarget target = getTargetBase().resolveBase("exchangeUID").resolve(uniqueId.toString());
    final Exchange exchange;
    try {
      exchange = getRestClient().getSingleValue(Exchange.class, target, "exchange");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (exchange == null) {
      throw new DataNotFoundException(target.toString());
    }
    return exchange;
  }

  @Override
  public Exchange getExchange(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final RestTarget target = getTargetBase().resolveBase("exchangeOID").resolveBase(objectId.toString()).resolveBase(versionCorrection.getVersionAsOfString()).resolve(
        versionCorrection.getCorrectedToString());
    final Exchange exchange;
    try {
      exchange = getRestClient().getSingleValue(Exchange.class, target, "exchange");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (exchange == null) {
      throw new DataNotFoundException(target.toString());
    }
    return exchange;
  }

  @Override
  public Collection<? extends Exchange> getExchanges(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final FudgeMsg msg = getRestClient().getMsg(
        getTargetBase().resolveBase("exchanges").resolveBase(versionCorrection.getVersionAsOfString()).resolveBase(versionCorrection.getCorrectedToString()).resolveQuery("id", bundle.toStringList()));
    if (msg == null) {
      throw new OpenGammaRuntimeException("Invalid server response");
    }
    final Collection<Exchange> result = new ArrayList<Exchange>(msg.getNumFields());
    final FudgeDeserializer fd = getRestClient().getFudgeDeserializer();
    for (FudgeField field : msg.getAllByName("exchange")) {
      result.add(fd.fieldValueToObject(Exchange.class, field));
    }
    return result;
  }

  @Override
  public Exchange getSingleExchange(final ExternalId identifier) {
    return getSingleExchange(ExternalIdBundle.of(identifier));
  }

  @Override
  public Exchange getSingleExchange(final ExternalIdBundle identifierBundle) {
    try {
      return getRestClient().getSingleValue(Exchange.class, getTargetBase().resolveBase("exchange").resolveQuery("id", identifierBundle.toStringList()), "exchange");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

}
