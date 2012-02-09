/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Tracks userData and clients
 */
public class DefaultFinancialUsersTracker implements FinancialUserDataTracker, FinancialClientTracker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultFinancialUsersTracker.class);

  private final ConcurrentMap<String, Set<String>> _username2clients = new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<ExternalId, Set<UniqueId>> _viewDefinitionIds = new ConcurrentHashMap<ExternalId, Set<UniqueId>>();
  private final ConcurrentMap<ExternalId, Set<UniqueId>> _marketDataSnapShots = new ConcurrentHashMap<ExternalId, Set<UniqueId>>();
  private final FinancialUserServices _services;
 
  public DefaultFinancialUsersTracker(FinancialUserServices services) {
    ArgumentChecker.notNull(services, "services");
    _services = services;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the services.
   * 
   * @return the services, not null
   */
  public FinancialUserServices getServices() {
    return _services;
  }

  //-------------------------------------------------------------------------
  @Override
  public void created(String userName, String clientName, FinancialUserDataType type, UniqueId identifier) {
    switch (type) {
      case VIEW_DEFINITION:
        trackCreatedViewDefinition(userName, clientName, identifier);
        break;
      case MARKET_DATA_SNAPSHOT:
        trackCreatedMarketDataSnapshot(userName, clientName, identifier);
        break;
    }
    Set<String> clients = _username2clients.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} created by {}", identifier, userName);
      }
    } else {
      s_logger.debug("Late creation of {} by {}", identifier, userName);
    }
  }

  private void trackCreatedMarketDataSnapshot(String userName, String clientName, UniqueId identifier) {
    ConcurrentSkipListSet<UniqueId> freshIds = new ConcurrentSkipListSet<UniqueId>();
    Set<UniqueId> marketDataSnapshotIds = _marketDataSnapShots.putIfAbsent(ExternalId.of(userName, clientName), freshIds);
    if (marketDataSnapshotIds == null) {
      marketDataSnapshotIds = freshIds;
    }
    freshIds.add(identifier);
    s_logger.debug("{} marketdatasnapshot created by {}", identifier, userName);
  }

  private void trackCreatedViewDefinition(String userName, String clientName, UniqueId identifier) {
    ConcurrentSkipListSet<UniqueId> freshDefinitions = new ConcurrentSkipListSet<UniqueId>();
    Set<UniqueId> viewDefinitions = _viewDefinitionIds.putIfAbsent(ExternalId.of(userName, clientName), freshDefinitions);
    if (viewDefinitions == null) {
      viewDefinitions = freshDefinitions;
    }
    viewDefinitions.add(identifier);
    s_logger.debug("{} view created by {}", identifier, userName);
  }

  @Override
  public void deleted(String userName, String clientName, FinancialUserDataType type, UniqueId identifier) {
    Set<String> clients = _username2clients.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} deleted by {}", identifier, userName);
        return;
      }
    }
    s_logger.debug("Late deletion of {} by {}", identifier, userName);
  }

  @Override
  public void clientCreated(String userName, String clientName) {
    Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      s_logger.debug("Late client construction for discarded user {}", userName);
      return;
    }
    clients.add(clientName);
    s_logger.debug("Client {} created for user {}", clientName, userName);
  }

  @Override
  public void clientDiscarded(String userName, String clientName) {
    Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      s_logger.debug("Late client discard for discarded user {}", userName);
    } else {
      clients.remove(clientName);
      s_logger.debug("Client {} discarded for user {}", clientName, userName);
    }
    removeUserViewDefinitions(userName, clientName);
    removeUserMarketDataSnapshot(userName, clientName);
  }

  private void removeUserMarketDataSnapshot(String userName, String clientName) {
    MarketDataSnapshotMaster marketDataSnapshotMaster = getServices().getSnapshotMaster();
    if (marketDataSnapshotMaster != null) {
      Set<UniqueId> snapshotIds = _marketDataSnapShots.remove(ExternalId.of(userName, clientName));
      for (UniqueId uid : snapshotIds) {
        marketDataSnapshotMaster.remove(uid);
        s_logger.debug("market data snapshot {} discarded for {}/{}", new Object[] {uid, userName, clientName});
      }
    }
  }
  
  private void removeAllUserMarketDataSnapshot(String userName) {
    MarketDataSnapshotMaster marketDataSnapshotMaster = getServices().getSnapshotMaster();
    if (marketDataSnapshotMaster != null) {
      Iterator<Entry<ExternalId, Set<UniqueId>>> iterator = _marketDataSnapShots.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<ExternalId, Set<UniqueId>> entry = iterator.next();
        ExternalId identifier = entry.getKey();
        if (identifier.getScheme().getName().equals(userName)) {
          Set<UniqueId> uids = entry.getValue();
          for (UniqueId uid : uids) {
            marketDataSnapshotMaster.remove(uid);
            s_logger.debug("market data snapshot {} discarded for {}/{}", new Object[] {uid, userName, identifier.getValue()});
          }
          iterator.remove();
        }
      }
    }
  }

  @Override
  public void userCreated(String userName) {
    Set<String> clients = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    _username2clients.putIfAbsent(userName, clients);
    s_logger.debug("User {} created", userName);
  }

  @Override
  public void userDiscarded(String userName) {
    Set<String> removedClients = _username2clients.remove(userName);
    s_logger.debug("User {} discarded", userName);
    if (removedClients != null) {
      for (String clientName : removedClients) {
        removeUserViewDefinitions(userName, clientName);
        removeUserMarketDataSnapshot(userName, clientName);
      }
    } else {
      removeAllUserViewDefinitions(userName);
      removeAllUserMarketDataSnapshot(userName);
    }
  }

  private void removeAllUserViewDefinitions(String userName) {
    ManageableViewDefinitionRepository viewDefinitionRepository = getServices().getViewDefinitionRepository();
    if (viewDefinitionRepository != null) {
      Iterator<Entry<ExternalId, Set<UniqueId>>> iterator = _viewDefinitionIds.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<ExternalId, Set<UniqueId>> entry = iterator.next();
        ExternalId identifier = entry.getKey();
        if (identifier.getScheme().getName().equals(userName)) {
          Set<UniqueId> viewDefinitions = entry.getValue();
          for (UniqueId viewDefinitionId : viewDefinitions) {
            viewDefinitionRepository.removeViewDefinition(viewDefinitionId);
            s_logger.debug("View definition {} discarded for {}/{}", new Object[] {viewDefinitionId, userName, identifier.getValue()});
          }
          iterator.remove();
        }
      }
    }
  }

  private void removeUserViewDefinitions(final String userName, final String clientName) {
    ManageableViewDefinitionRepository viewDefinitionRepository = getServices().getViewDefinitionRepository();
    if (viewDefinitionRepository != null) {
      Set<UniqueId> viewDefinitions = _viewDefinitionIds.remove(ExternalId.of(userName, clientName));
      for (UniqueId viewDefinitionId : viewDefinitions) {
        viewDefinitionRepository.removeViewDefinition(viewDefinitionId);
        s_logger.debug("View definition {} discarded for {}/{}", new Object[] {viewDefinitionId, userName, clientName});
      }
    }
  }

}
