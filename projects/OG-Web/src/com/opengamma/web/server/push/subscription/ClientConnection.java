/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

import java.util.HashMap;
import java.util.Map;

/**
 * Associated with one client connection (i.e. one browser window / tab / client app instance).
 */
public class ClientConnection implements ChangeListener {

  private final String _userId;
  private final String _clientId;
  private final RestUpdateListener _listener;
  private final ViewportFactory _viewportFactory;
  private final Object _lock = new Object();

  /** REST URLs for entities keyed on the entity's {@link UniqueId} */
  private final Map<ObjectId, String> _entityUrls = new HashMap<ObjectId, String>();

  private Viewport _viewport;
  private String _viewportUrl;
  private boolean _closed = false;

  public ClientConnection(String userId, String clientId, RestUpdateListener listener, ViewportFactory viewportFactory) {
    // TODO check args
    _viewportFactory = viewportFactory;
    _userId = userId;
    _listener = listener;
    _clientId = clientId;
  }

  private void checkClosed() {
    if (_closed) {
      throw new IllegalStateException("Connection is closed");
    }
  }

  public String getClientId() {
    synchronized (_lock) {
      checkClosed();
      return _clientId;
    }
  }

  public String getUserId() {
    synchronized (_lock) {
      checkClosed();
      return _userId;
    }
  }

  /**
   * Creates a new subscription for a view client, replacing any existing subscription for that view client.
   * @param viewportDefinition
   */
  public void createViewport(ViewportDefinition viewportDefinition, String viewportUrl, String dataUrl, String gridUrl) {
    synchronized (_lock) {
      checkClosed();
      _viewportUrl = viewportUrl;
      AnalyticsListener listener = new AnalyticsListener(dataUrl, gridUrl, _listener);
      _viewport = _viewportFactory.createViewport(_clientId, viewportDefinition, listener);
      _viewportUrl = viewportUrl;
    }
  }

  public void disconnect() {
    synchronized (_lock) {
      _entityUrls.clear();
      _closed = true;
      // TODO can these safely move outside the sync block? do they need to? probably not
      _viewport.close();
      _viewportFactory.clientDisconnected(_clientId);
    }
  }

  public void subscribe(UniqueId uid, String url) {
    // TODO check args?
    synchronized (_lock) {
      checkClosed();
      _entityUrls.put(uid.getObjectId(), url);
    }
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    synchronized (_lock) {
      checkClosed();
      String url = _entityUrls.get(event.getAfterId().getObjectId());
      // TODO could this move outside the sync block?
      if (url != null) {
        _listener.itemUpdated(url);
      }
    }
  }

  public Viewport getViewport(String viewportUrl) {
    synchronized (_lock) {
      checkClosed();
      if (!_viewportUrl.equals(viewportUrl)) {
        throw new IllegalArgumentException("Invalid viewport URL: " + viewportUrl + ", current URL: " + _viewportUrl);
      }
      return _viewport;
    }
  }

  public String getViewportUrl() {
    synchronized (_lock) {
      checkClosed();
      return _viewportUrl;
    }
  }
}

