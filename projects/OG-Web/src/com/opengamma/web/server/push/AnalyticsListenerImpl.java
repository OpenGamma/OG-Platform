package com.opengamma.web.server.push;

/**
 * {@link AnalyticsListener} that notifies a {@link RestUpdateListener} of changes to the analytics.
 */
/* package */ class AnalyticsListenerImpl implements AnalyticsListener {

  private final String _dataUrl;
  private final String _gridStructureUrl;
  private final RestUpdateListener _listener;

  /**
   * @param dataUrl REST URL passed to {@link RestUpdateListener#itemUpdated(String)} when the
   * view's analytics data changes
   * @param gridStructureUrl REST URL passed to {@link RestUpdateListener#itemUpdated(String)} when
   * the structure of any of the view's grids changes
   * @param listener Listener to which updates are forwarded
   */
  /* package */ AnalyticsListenerImpl(String dataUrl, String gridStructureUrl, RestUpdateListener listener) {
    _dataUrl = dataUrl;
    _gridStructureUrl = gridStructureUrl;
    _listener = listener;
  }

  @Override
  public void dataChanged() {
    _listener.itemUpdated(_dataUrl);
  }

  @Override
  public void gridStructureChanged() {
    _listener.itemUpdated(_gridStructureUrl);
  }
}
