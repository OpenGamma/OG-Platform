package com.opengamma.web.server.push.subscription;

/**
 *
 */
public class AnalyticsListener {

  private final String _dataUrl;
  private final String _gridStructureUrl;
  private final RestUpdateListener _listener;

  private final Object _lock = new Object();
  private boolean _active = false;

  public AnalyticsListener(String dataUrl, String gridStructureUrl, RestUpdateListener listener) {
    _dataUrl = dataUrl;
    _gridStructureUrl = gridStructureUrl;
    _listener = listener;
  }

  public void dataChanged() {
    synchronized (_lock) {
      if (_active) {
        _active = false;
        _listener.itemUpdated(_dataUrl);
      }
    }
  }

  public void gridStructureChanged() {
    synchronized (_lock) {
      _listener.itemUpdated(_gridStructureUrl);
    }
  }

  public void activate() {
    synchronized (_lock) {
      _active = true;
    }
  }
}
