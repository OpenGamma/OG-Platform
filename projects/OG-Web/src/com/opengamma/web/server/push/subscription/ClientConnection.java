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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Associated with one client connection (i.e. one browser window / tab / client app instance). Can be associated
 * with any number of subscriptions for view and entity data.
 * TODO does this need multiple subscriptions? is one view client subscription sufficient?
 * TODO CONCURRENCY - external? internal via locks? actor style (Runnables / Callables / Futures?)
 */
public class ClientConnection implements ChangeListener {

  private final String _clientId;
  private final SubscriptionListener _listener;
  // TODO what collection type? concurrency
  // if the entity subs are MasterChangeListeners then this class never needs to look them up, they can notify the SubscriptionListener
  // the only time this class needs to refer to them is to dispose of them if they're alive when the connection closes
  private final Collection<Subscription> _entitySubscriptions = new ArrayList<Subscription>();
  private final String _userId;
  private final ViewportFactory _viewportFactory;

  // TODO should this be a concurrent map?
  /** REST URLs for entities keyed on the entity's {@link UniqueId} */
  private final HashMap<ObjectId, String> _entityUrls = new HashMap<ObjectId, String>();

  // TODO atomic ref?
  private Viewport _viewport;

  public ClientConnection(String userId, String clientId, SubscriptionListener listener, ViewportFactory viewportFactory) {
    // TODO check args
    _viewportFactory = viewportFactory;
    _userId = userId;
    _listener = listener;
    _clientId = clientId;
  }

  public String getClientId() {
    return _clientId;
  }

  public String getUserId() {
    return _userId;
  }

  /**
   * Creates a new subscription for a view client, replacing any existing subscription for that view client.
   * @param request
   * TODO refactor so the stack isn't so deep when setting up new subs? create everything in the subs manager? is that workable?
   * TODO logic in subscription requests? command pattern?
   */
  public void createViewportSubscription(ViewportSubscriptionRequest request) {
    UniqueId viewClientId = request.getViewClientId();
    _viewportFactory.createViewport(viewClientId, request.getViewportBounds(), _listener);
  }

  public void activateViewportSubscription(String viewportId) {
    // TODO implement ClientConnection.activateViewClientSubscription()
    throw new UnsupportedOperationException("activateViewClientSubscription not implemented");
  }

  public void disconnect() {
    // TODO dispose of all the subscriptions
  }

  public void subscribe(UniqueId uid, String url) {
    // TODO check args?
    _entityUrls.put(uid.getObjectId(), url);
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    String url = _entityUrls.get(event.getAfterId().getObjectId());
    if (url != null) {
      _listener.itemUpdated(url);
    }
  }
}
