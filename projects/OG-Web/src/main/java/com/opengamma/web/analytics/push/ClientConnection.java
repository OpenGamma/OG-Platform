/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Connection associated with one client (i.e. one browser window / tab / client app instance). Allows subscriptions
 * to be set up so the client is notified if an entity or the contents of a master changes.
 * The published notifications contain the REST URL of the thing that has changed.
 * All subscriptions for a URL are automatically cancelled the first time a notification is published for the URL
 * and must be re-established every time the client accesses the URL.  This class is thread safe.
 * TODO should this be package-private and everything moved into the same package?
 */
public class ClientConnection implements ChangeListener, MasterChangeListener, UpdateListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ClientConnection.class);
  
  /** Login ID of the user that owns this connection TODO this isn't used yet */
  private final String _userId;
  /** Unique ID of this connection */
  private final String _clientId;
  /** Task that closes this connection if it is idle for too long */
  private final ConnectionTimeoutTask _timeoutTask;
  /** Listener that forwards changes over HTTP whenever any updates occur to which this connection subscribes */
  private final UpdateListener _listener;
  /** Listeners that are called when this connection closes. */
  private final List<DisconnectionListener> _disconnectionListeners = new CopyOnWriteArrayList<DisconnectionListener>();

  /** Lock which must be held when mutating any of the objects below */
  private final Object _lock = new Object();
  /** URLs which should be published when a master changes, keyed by the type of the master */
  private final Multimap<MasterType, String> _masterUrls = HashMultimap.create();
  /** URLs which should be published when an entity changes, keyed on the entity's ID */
  private final Multimap<ObjectId, String> _entityUrls = HashMultimap.create();
  /** Map of URLs for which changes should be published to their underlying objects. */
  private final Map<String, UrlMapping> _urlMappings = new HashMap<String, UrlMapping>();
  /** Connection flag. */
  private boolean _connected = true;

  /**
   * @param userId Login ID of the user that owns this connection, null if not known
   * @param clientId Unique ID of this connection 
   * @param listener Listener that forwards changes over HTTP whenever any updates occur to which this connection subscribes 
   * @param timeoutTask Task that closes this connection if it is idle for too long
   */
  /* package */ ClientConnection(String userId,
                                 String clientId,
                                 UpdateListener listener,
                                 ConnectionTimeoutTask timeoutTask) {
    ArgumentChecker.notNull(listener, "listener");
    ArgumentChecker.notNull(clientId, "clientId");
    ArgumentChecker.notNull(timeoutTask, "timeoutTask");
    s_logger.debug("Creating new client connection. userId: {}, clientId: {}", userId, clientId);
    _userId = userId;
    _listener = listener;
    _clientId = clientId;
    _timeoutTask = timeoutTask;
  }

  /**
   * @return Login ID of the user that owns this connection
   */
  /* package */ String getUserId() {
    return _userId;
  }

  /**
   * Disconnects this client.
   */
  /* package */ void disconnect() {
    s_logger.debug("Disconnecting client connection, userId: {}, clientId: {}", _userId, _clientId);
    synchronized (_lock) {
      _connected = false;
      _timeoutTask.cancel();
      for (DisconnectionListener listener : _disconnectionListeners) {
        try {
          listener.clientDisconnected();
        } catch (Exception e) {
          s_logger.warn("Problem calling disconnection listener", e);
        }
      }
    }
  }

  /**
   * Sets up a subscription that publishes an update to the client when an entity changes.
   * The subscription is automatically cancelled after the first time the entity changes.
   * @param uid The unique ID of an entity
   * @param url The REST URL of the entity.  This is published to the client when the entity is updated
   */
  /* package */ void subscribe(UniqueId uid, String url) {
    ArgumentChecker.notNull(uid, "uid");
    ArgumentChecker.notNull(url, "url");
    s_logger.debug("Client ID {} subscribing for changes to {}, URL: {}", new Object[]{_clientId, uid, url});
    synchronized (_lock) {
      _timeoutTask.reset();
      ObjectId objectId = uid.getObjectId();
      _entityUrls.put(objectId, url);
      _urlMappings.put(url, UrlMapping.create(_urlMappings.get(url), objectId));
    }
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    s_logger.debug("Received ChangeEvent {}", event);
    synchronized (_lock) {
      ObjectId objectId = event.getObjectId();      
      Collection<String> urls = _entityUrls.removeAll(objectId);
      removeSubscriptions(urls);
      if (!urls.isEmpty()) {
        _listener.itemsUpdated(urls);
      }
    }
  }

  /**
   * Sets up a subscription that publishes an update to the client when any entity in a master changes.
   * This tells a client that the results of a previously executed query <em>might</em> have changed.
   * The subscription is automatically cancelled after the first time the master is updated.
   * @param masterType The type of master
   * @param url The REST URL whose results might be invalidated by changes in the master
   */
  /* package */ void subscribe(MasterType masterType, String url) {
    ArgumentChecker.notNull(masterType, "masterType");
    ArgumentChecker.notNull(url, "url");
    s_logger.debug("Subscribing to notifications for changes to {} master, notification URL: {}", masterType, url);
    synchronized (_lock) {
      _timeoutTask.reset();
      _masterUrls.put(masterType, url);
      _urlMappings.put(url, UrlMapping.create(_urlMappings.get(url), masterType));
    }
  }

  @Override
  public void masterChanged(MasterType masterType) {
    s_logger.debug("Received notification {} master changed", masterType);
    synchronized (_lock) {
      Collection<String> urls = _masterUrls.removeAll(masterType);
      removeSubscriptions(urls);
      if (!urls.isEmpty()) {
        _listener.itemsUpdated(urls);
      }
    }
  }

  /**
   * Removes all subscriptions for the URLs.  When an update is published for a URL all subscriptions for that
   * URL for all {@link MasterType}s or entity {@link ObjectId}s are cancelled.
   * @param urls The URLs for which updates have been published
   */
  private void removeSubscriptions(Collection<String> urls) {
    for (String url : urls) {
      UrlMapping urlMapping = _urlMappings.get(url);
      // remove mappings for this url for master type
      for (MasterType type : urlMapping.getMasterTypes()) {
        _masterUrls.remove(type, url);
      }
      // remove mappings for this url for all entities
      for (ObjectId entityId : urlMapping.getEntityIds()) {
        _entityUrls.remove(entityId, url);
      }
    }
  }

  @Override
  public void itemUpdated(Object callbackId) {
    _listener.itemUpdated(callbackId);
  }

  @Override
  public void itemsUpdated(Collection<?> callbackIds) {
    _listener.itemsUpdated(callbackIds);
  }

  /**
   * Adds a listener that will be notified when the client disconnects. If this is called after the client has
   * disconnected the listener will be called immediately.
   * @param listener The listener
   */
  public void addDisconnectionListener(DisconnectionListener listener) {
    synchronized (_lock) {
      if (_connected) {
        _disconnectionListeners.add(listener);
      } else {
        listener.clientDisconnected();
      }
    }
  }

  /**
   * <p>Container for sets of {@link MasterType}s or {@link ObjectId}s associated with a subscription for a REST URL.
   * This is to allow all subscriptions for a URL to be cleared when its first update is published.</p>
   * <p>This assumes there can be multiple subscriptions for a URL with different {@link MasterType}s or
   * entity {@link ObjectId}s.  TODO Need to check whether this is actually the case.
   * If not this could probably be scrapped.</p>
   */
  private static final class UrlMapping {

    private final Set<MasterType> _masterTypes;
    private final Set<ObjectId> _entityIds;

    private UrlMapping(Set<MasterType> masterTypes, Set<ObjectId> entityIds) {
      _masterTypes = masterTypes;
      _entityIds = entityIds;
    }

    private Set<MasterType> getMasterTypes() {
      return _masterTypes;
    }

    private Set<ObjectId> getEntityIds() {
      return _entityIds;
    }

    private static UrlMapping create(UrlMapping urlMapping, MasterType masterType) {
      if (urlMapping == null) {
        return new UrlMapping(ImmutableSet.of(masterType), Collections.<ObjectId>emptySet());
      } else {
        ImmutableSet<MasterType> masterTypes =
            ImmutableSet.<MasterType>builder().addAll(urlMapping.getMasterTypes()).add(masterType).build();
        return new UrlMapping(masterTypes, urlMapping.getEntityIds());
      }
    }

    private static UrlMapping create(UrlMapping urlMapping, ObjectId entityId) {
      if (urlMapping == null) {
        return new UrlMapping(Collections.<MasterType>emptySet(), ImmutableSet.of(entityId));
      } else {
        ImmutableSet<ObjectId> entityIds =
            ImmutableSet.<ObjectId>builder().addAll(urlMapping.getEntityIds()).add(entityId).build();
        return new UrlMapping(urlMapping.getMasterTypes(), entityIds);
      }
    }
  }

  /**
   * Listeners are called when a connection disconnects.
   */
  public interface DisconnectionListener {

    /**
     * Called when the {@link ClientConnection} disconnects.
     */
    void clientDisconnected();
  }
}

