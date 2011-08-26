package com.opengamma.web.server.push.subscription;

/**
 * TODO the semantics aren't quite right - should discard updates when inactive for consitency with entity subs
 */
public class AnalyticsListener {

  private final String _dataUrl;
  private final String _gridStructureUrl;
  private final RestUpdateListener _listener;

  private final Object _lock = new Object();
  private boolean _active = false; // TODO what should the default be?
  private boolean _dataChanged = false;

  public AnalyticsListener(String dataUrl, String gridStructureUrl, RestUpdateListener listener) {
    _dataUrl = dataUrl;
    _gridStructureUrl = gridStructureUrl;
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

  public void gridStructureChanged() {
    synchronized (_lock) {
      _listener.itemUpdated(_gridStructureUrl);
    }
  }

  public void activate() {
    synchronized (_lock) {
      if (_dataChanged) {
        fireUpdate();
      } else {
        _active = true;
      }
    }
  }

  private void fireUpdate() {
    _active = false;
    _dataChanged = false;
    _listener.itemUpdated(_dataUrl);
  }
}
