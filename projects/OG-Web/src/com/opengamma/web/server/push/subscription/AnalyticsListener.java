package com.opengamma.web.server.push.subscription;

/**
 *
 */
public class AnalyticsListener {

  private final String _dataUrl;
  private final String _gridStructureUrl;
  private final RestUpdateListener _listener;

  public AnalyticsListener(String dataUrl, String gridStructureUrl, RestUpdateListener listener) {
    _dataUrl = dataUrl;
    _gridStructureUrl = gridStructureUrl;
    _listener = listener;
  }

  public void dataChanged() {
    _listener.itemUpdated(_dataUrl);
  }

  public void gridStructureChanged() {
    _listener.itemUpdated(_gridStructureUrl);
  }
}
