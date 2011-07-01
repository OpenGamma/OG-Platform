/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.annotations.Sets;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;

/**
 * A {@link AbstractLiveDataServer} which delegates all the work to a set of {@link AbstractLiveDataServer} 
 * NOTE: this is only really a partial implementation of AbstractLiveDataServer
 *        e.g. ExpirationManagers and will need to point at the underlyings 
 */
public abstract class CombiningLiveDataServer extends AbstractLiveDataServer {
  //TODO: things include Entitlement checking, heartbeats
  
  private static final Logger s_logger = LoggerFactory.getLogger(CombiningLiveDataServer.class);

  private final Set<AbstractLiveDataServer> _underlyings;

  public CombiningLiveDataServer(AbstractLiveDataServer... otherUnderlyings) {
    this(Arrays.asList(otherUnderlyings));
  }

  public CombiningLiveDataServer(Collection<? extends AbstractLiveDataServer> otherUnderlyings) {
    _underlyings = Sets.newHashSet();
    _underlyings.addAll(otherUnderlyings);

  }

  @Override
  public LiveDataSubscriptionResponseMsg subscriptionRequestMadeImpl(LiveDataSubscriptionRequest subscriptionRequest) {
    List<LiveDataSpecification> specs = subscriptionRequest.getSpecifications();
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> mapped = groupByServer(specs);

    Collection<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>(
        subscriptionRequest.getSpecifications().size());

    //TODO: should probably be asynchronous 
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : mapped.entrySet()) {
      if (entry.getValue().isEmpty()) {
        continue;
      }
      LiveDataSubscriptionRequest liveDataSubscriptionRequest = new LiveDataSubscriptionRequest(
          subscriptionRequest.getUser(), subscriptionRequest.getType(), entry.getValue());
      AbstractLiveDataServer server = entry.getKey();
      s_logger.debug("Sending subscription requests for {} to underlying server {}", liveDataSubscriptionRequest,
          server);
      //NOTE: we call up to subscriptionRequestMade to get the exception catching
      LiveDataSubscriptionResponseMsg response = server.subscriptionRequestMade(liveDataSubscriptionRequest);

      //Check that we know how to combine these responses
      if (response.getRequestingUser() != subscriptionRequest.getUser()) {
        throw new OpenGammaRuntimeException("Unexpected user in response " + response.getRequestingUser());
      }
      responses.addAll(response.getResponses());
    }
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUser(), responses);
  }

  protected abstract Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      List<LiveDataSpecification> specs);

  @Override
  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    for (AbstractLiveDataServer server : _underlyings) {
      server.addSubscriptionListener(subscriptionListener);
    }
  }

  @Override
  protected void doConnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.doConnect();
    }
  }

  @Override
  protected void doDisconnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.doDisconnect();
    }
  }

  @Override
  public void setMarketDataSenderFactory(MarketDataSenderFactory marketDataSenderFactory) {
    for (AbstractLiveDataServer server : _underlyings) {
      server.setMarketDataSenderFactory(marketDataSenderFactory);
    }
  }

  //----- Shouldn't happen -----
  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    throw new IllegalArgumentException();
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected IdentificationScheme getUniqueIdDomain() {
    throw new IllegalArgumentException();
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    throw new IllegalArgumentException();
  }
}
