package com.opengamma.web.server.push;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.reports.ViewportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TestViewportManager implements ViewportManager {

  private static final Logger s_logger = LoggerFactory.getLogger(TestViewportManager.class);
  private static final Object[][] s_dummyData = {{1, 2, 3}, {2, 4, 6}, {3, 6, 9}, {4, 8, 12}};

  private final AtomicReference<Viewport> _viewport = new AtomicReference<Viewport>();

  @Override
  public Viewport createViewport(String viewportId, String previousViewportId,
                                 ViewportDefinition viewportDefinition,
                                 AnalyticsListener listener) {
    _viewport.set(new TestViewport(viewportDefinition));
    return _viewport.get();
  }

  @Override
  public Viewport getViewport(String viewportId) {
    return _viewport.get();
  }

  @Override
  public void closeViewport(String viewportId) {
    // do nothing
  }

  @Override
  public Viewport createViewport(String viewportId, ViewportDefinition viewportDefinition) {
    throw new UnsupportedOperationException("createViewport not implemented");
  }

  private static class TestViewport implements Viewport {

    private final Map<String, Object> _latestData = new HashMap<String, Object>();

    public TestViewport(ViewportDefinition viewportDefinition) {
      _latestData.clear();
      for (Map.Entry<Integer, Long> entry : viewportDefinition.getPortfolioRows().entrySet()) {
        Integer rowId = entry.getKey();
        Object[] rowData = s_dummyData[rowId];
        _latestData.put(Integer.toString(rowId), rowData);
      }
    }

    @Override
    public Map<String, Object> getGridStructure() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getLatestResults() {
      return _latestData;
    }

    @Override
    public void setRunning(boolean run) {
      s_logger.info("setRunning(), run: {}", run);
    }

    @Override
    public ViewportData getRawData() {
      throw new UnsupportedOperationException("getRawData not implemented");
    }
  }
}
