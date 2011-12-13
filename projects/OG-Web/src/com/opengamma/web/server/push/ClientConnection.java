/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.rest.MasterType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Connection associated with one client (i.e. one browser window / tab / client app instance).
 */
/* package */ class ClientConnection implements ChangeListener, MasterChangeListener {

  /** Login ID of the user that owns this connection TODO this isn't used yet */
  private final String _userId;
  /** Unique ID of this connection */
  private final String _clientId;
  /** Task that closes this connection if it is idle for too long */
  private final ConnectionTimeoutTask _timeoutTask;
  /** Listener that forwards changes over HTTP whenever any updates occur to which this connection subscribes */
  private final RestUpdateListener _listener;
  /** For creating and returning viewports on the analytics data */
  private final ViewportManager _viewportFactory;
  private final Object _lock = new Object();

  private final Multimap<MasterType, String> _masterUrls = HashMultimap.create();
  private final Multimap<ObjectId, String> _entityUrls = HashMultimap.create();
  private final Map<String, UrlMapping> _urlMappings = new HashMap<String, UrlMapping>();

  private String _viewportId;

  /* package */ ClientConnection(String userId,
                                 String clientId,
                                 RestUpdateListener listener,
                                 ViewportManager viewportFactory,
                                 ConnectionTimeoutTask timeoutTask) {
    // TODO check args
    _viewportFactory = viewportFactory;
    _userId = userId;
    _listener = listener;
    _clientId = clientId;
    _timeoutTask = timeoutTask;
  }

  /* package */ String getUserId() {
    return _userId;
  }

  /**
   * Creates a new subscription for a view client, replacing any existing subscription for that view client.
   * @param viewportDefinition
   * @param viewportId
   * @param dataUrl
   * @param gridUrl
   */
  /* package */ void createViewport(ViewportDefinition viewportDefinition, String viewportId, String dataUrl, String gridUrl) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(viewportId, "viewportId");
    ArgumentChecker.notNull(dataUrl, "dataUrl");
    ArgumentChecker.notNull(gridUrl, "gridUrl");
    synchronized (_lock) {
      _timeoutTask.reset();
      String previousViewportId = _viewportId;
      _viewportId = viewportId;
      AnalyticsListener listener = new AnalyticsListenerImpl(dataUrl, gridUrl, _listener);
      _viewportFactory.createViewport(viewportId, previousViewportId, viewportDefinition, listener);
    }
  }

  /* package */ Viewport getViewport(String viewportId) {
    synchronized (_lock) {
      _timeoutTask.reset();
      if (!_viewportId.equals(viewportId)) {
        throw new DataNotFoundException("Viewport ID " + viewportId + " not assiociated with client ID " + _clientId);
      }
      return _viewportFactory.getViewport(_viewportId);
    }
  }

  /* package */ void disconnect() {
    synchronized (_lock) {
      _timeoutTask.reset();
      _viewportFactory.closeViewport(_viewportId);
    }
  }

  /* package */ void subscribe(UniqueId uid, String url) {
    synchronized (_lock) {
      _timeoutTask.reset();
      ObjectId objectId = uid.getObjectId();
      _entityUrls.put(objectId, url);
      _urlMappings.put(url, UrlMapping.create(_urlMappings.get(url), objectId));
    }
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    synchronized (_lock) {
      Collection<String> urls = _entityUrls.removeAll(event.getAfterId().getObjectId());
      removeUrlMappings(urls);
      if (!urls.isEmpty()) {
        _listener.itemsUpdated(urls);
      }
    }
  }

  /* package */ void subscribe(MasterType masterType, String url) {
    synchronized (_lock) {
      _timeoutTask.reset();
      _masterUrls.put(masterType, url);
      _urlMappings.put(url, UrlMapping.create(_urlMappings.get(url), masterType));
    }
  }

  @Override
  public void masterChanged(MasterType masterType) {
    synchronized (_lock) {
      Collection<String> urls = _masterUrls.removeAll(masterType);
      removeUrlMappings(urls);
      if (!urls.isEmpty()) {
        _listener.itemsUpdated(urls);
      }
    }
  }

  // TODO a better name
  private void removeUrlMappings(Collection<String> urls) {
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

  // TODO this is a rubbish name
  private static class UrlMapping {

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
}

