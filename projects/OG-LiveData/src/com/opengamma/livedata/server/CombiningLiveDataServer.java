/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link AbstractLiveDataServer} which delegates all the work to a set of {@link AbstractLiveDataServer} 
 * NOTE: this is only really a partial implementation of AbstractLiveDataServer
 *        e.g. Entitlement checking will have to be set up on this client as well as on the underlyings 
 */
public abstract class CombiningLiveDataServer extends AbstractLiveDataServer {
  //TODO: things include Entitlement checking
  
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
  public Collection<LiveDataSubscriptionResponse> subscribe(Collection<LiveDataSpecification> liveDataSpecificationsFromClient, final boolean persistent) {
    return subscribeByServer(
        liveDataSpecificationsFromClient,
        new SubscribeAction() {

          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications) {
            return  server.subscribe(specifications, persistent);
          }
          
          @Override
          public String getName() {
            return "Subscribe";
          }
        });
  }

  @Override
  public LiveDataSubscriptionResponseMsg subscriptionRequestMadeImpl(final LiveDataSubscriptionRequest subscriptionRequest) {
    //Need to override here as well in order to catch the resolution/entitlement checking
    
    Collection<LiveDataSubscriptionResponse> responses = subscribeByServer(
        subscriptionRequest.getSpecifications(),
        new SubscribeAction() {
          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications) {
            LiveDataSubscriptionRequest liveDataSubscriptionRequest = buildSubRequest(subscriptionRequest, specifications);
            //NOTE: we call up to subscriptionRequestMade to get the exception catching
            LiveDataSubscriptionResponseMsg response = server.subscriptionRequestMade(liveDataSubscriptionRequest);

            //Check that we know how to combine these responses
            if (response.getRequestingUser() != subscriptionRequest.getUser()) {
              throw new OpenGammaRuntimeException("Unexpected user in response " + response.getRequestingUser());
            }
            return response.getResponses();
          }

          @Override
          public String getName() {
            return "SubscriptionRequestMade";
          }
        });
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUser(), responses);
  }
  
  private LiveDataSubscriptionRequest buildSubRequest(final LiveDataSubscriptionRequest subscriptionRequest, Collection<LiveDataSpecification> specifications) {
    LiveDataSubscriptionRequest liveDataSubscriptionRequest = new LiveDataSubscriptionRequest(subscriptionRequest.getUser(), subscriptionRequest.getType(), specifications);
    return liveDataSubscriptionRequest;
  }
  
  private interface SubscribeAction {
    Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications);
    String getName();
  }
  private Collection<LiveDataSubscriptionResponse> subscribeByServer(Collection<LiveDataSpecification> specifications, final SubscribeAction action)
  {
    return forEachServer(specifications, new Function<Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>>, Collection<LiveDataSubscriptionResponse>>() {
      @Override
      public Collection<LiveDataSubscriptionResponse> apply(Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>> input) {
        AbstractLiveDataServer specs = input.getFirst();
        Collection<LiveDataSpecification> server = input.getSecond();
        s_logger.debug("Sending subscription ({}) for {} to underlying server {}", new Object[] {action.getName(), specs, server});
        return action.subscribe(specs, server);
      }
    });
  }
  private <T> Collection<T> forEachServer(Collection<LiveDataSpecification> specifications, Function<Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>>, Collection<T>> operation)
  {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> mapped = groupByServer(specifications);

    Collection<T> responses = new ArrayList<T>(specifications.size());

    //TODO: should probably be asynchronous 
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : mapped.entrySet()) {
      if (entry.getValue().isEmpty()) {
        continue;
      }
      Collection<T> partitionResponse = operation.apply(Pair.of(entry.getKey(), entry.getValue()));
      
      responses.addAll(partitionResponse);
    }
    return responses;
  }

  protected abstract Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      Collection<LiveDataSpecification> specs);

  private AbstractLiveDataServer getServer(LiveDataSpecification spec) {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(Sets.newHashSet(spec));
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      if (entry.getValue().size() > 0) {
        return entry.getKey();
      }
    }
    throw new OpenGammaRuntimeException("Couldn't find server for " + spec);
  }
  
  // For expiration manager
  @Override
  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    for (AbstractLiveDataServer server : _underlyings) {
      server.addSubscriptionListener(subscriptionListener);
    }
  }

  
  @Override
  public Set<Subscription> getSubscriptions() {
    Set<Subscription> ret = new HashSet<Subscription>();
    for (AbstractLiveDataServer server : _underlyings) {
      Set<Subscription> serversSubscriptions = server.getSubscriptions();
      s_logger.debug("Server {} has {} subscriptions", server, serversSubscriptions.size());
      ret.addAll(serversSubscriptions);
    }
    return ret;
  }

  
  @Override
  public Subscription getSubscription(LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getSubscription(fullyQualifiedSpec);
  }

  @Override
  public MarketDataDistributor getMarketDataDistributor(LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getMarketDataDistributor(fullyQualifiedSpec);
  }
  
  
  @Override
  public Map<LiveDataSpecification, MarketDataDistributor> getMarketDataDistributors(Collection<LiveDataSpecification> fullyQualifiedSpecs) {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(fullyQualifiedSpecs);
    HashMap<LiveDataSpecification, MarketDataDistributor> ret = new HashMap<LiveDataSpecification, MarketDataDistributor>();
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      Map<LiveDataSpecification, MarketDataDistributor> entries = entry.getKey().getMarketDataDistributors(entry.getValue());
      ret.putAll(entries);
    }
    return ret;
  }

  @Override
  public boolean stopDistributor(MarketDataDistributor distributor) {
    return getServer(distributor.getFullyQualifiedLiveDataSpecification()).stopDistributor(distributor);    
  }

  @Override
  protected void doConnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.start();
    }
  }

  @Override
  protected void doDisconnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.stop();
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
  protected ExternalScheme getUniqueIdDomain() {
    throw new IllegalArgumentException();
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    throw new IllegalArgumentException();
  }
}
