/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Associated with one client connection (i.e. one browser window / tab / client app instance).
 * TODO current scheme is no good, need to remove entity and master subscriptions for a URL if either of them fire
 */
/* package */ class ClientConnection implements ChangeListener, MasterChangeListener {

  private final String _userId;
  private final String _clientId;
  private final RestUpdateListener _listener;
  private final ViewportFactory _viewportFactory;
  private final Object _lock = new Object();

  /** REST URLs for entities keyed on the entity's {@link ObjectId} */
  private final Map<ObjectId, String> _entityUrls = new ConcurrentHashMap<ObjectId, String>();

  private final Multimap<MasterType, String> _masterUrls = HashMultimap.create();

  /* package */ ClientConnection(String userId, String clientId, RestUpdateListener listener, ViewportFactory viewportFactory) {
    // TODO check args
    _viewportFactory = viewportFactory;
    _userId = userId;
    _listener = listener;
    _clientId = clientId;
  }

  /* package */ String getClientId() {
    return _clientId;
  }

  /* package */ String getUserId() {
    return _userId;
  }

  /**
   * Creates a new subscription for a view client, replacing any existing subscription for that view client.
   * @param viewportDefinition
   */
  /* package */ void createViewport(ViewportDefinition viewportDefinition, String viewportUrl, String dataUrl, String gridUrl) {
    AnalyticsListener listener = new AnalyticsListener(dataUrl, gridUrl, _listener);
    _viewportFactory.createViewport(_clientId, viewportUrl, viewportDefinition, listener);
  }

  /* package */ void disconnect() {
    _entityUrls.clear();
    _viewportFactory.clientDisconnected(_clientId);
  }

  /* package */ void subscribe(UniqueId uid, String url) {
    // TODO check args?
    _entityUrls.put(uid.getObjectId(), url);
  }

  /* package */ void subscribe(MasterType masterType, String url) {
    synchronized (_lock) {
      _masterUrls.put(masterType, url);
    }
  }

  /* package */ Viewport getViewport(String viewportUrl) {
    return _viewportFactory.getViewport(viewportUrl);
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    String url = _entityUrls.remove(event.getAfterId().getObjectId());
    if (url != null) {
      _listener.itemUpdated(url);
    }
  }

  @Override
  public void masterChanged(MasterType masterType) {
    synchronized (_lock) {
      Collection<String> urls = _masterUrls.get(masterType);
      if (!urls.isEmpty()) {
        _listener.itemsUpdated(urls);
      }
    }
  }
}

