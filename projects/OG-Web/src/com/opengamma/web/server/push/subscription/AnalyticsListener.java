package com.opengamma.web.server.push.subscription;

/**
 *
 */
public class AnalyticsListener {

  private final String _viewportUrl;
  private final RestUpdateListener _listener;
  private final Object _lock = new Object();

  private boolean _active = false; // TODO what should the default be?
  private boolean _dataChanged = false;

  public AnalyticsListener(String viewportUrl, RestUpdateListener listener) {
    _viewportUrl = viewportUrl;
    _listener = listener;
  }

  public void dataChanged() {
    synchronized (_lock) {
      if (_active) {
        fireUpdate();
      } else {
        _dataChanged = true;
      }
    }
  }

  void activate() {
    synchronized (_lock) {
      if (_dataChanged) {
        fireUpdate();
      } else {
        _dataChanged = true;
      }
    }
  }

  private void fireUpdate() {
    _active = false;
    _dataChanged = false;
    _listener.itemUpdated(_viewportUrl);
  }
}
