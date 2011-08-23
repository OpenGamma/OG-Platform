package com.opengamma.web.server.push.web;

import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.push.subscription.AnalyticsListener;
import com.opengamma.web.server.push.subscription.Viewport;
import com.opengamma.web.server.push.subscription.ViewportDefinition;
import com.opengamma.web.server.push.subscription.ViewportFactory;
import com.opengamma.web.server.push.subscription.ViewportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestViewportFactory implements ViewportFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(TestViewportFactory.class);
  private static final Object[][] s_dummyData = {{1, 2, 3}, {2, 4, 6}, {3, 6, 9}, {4, 8, 12}};

  @Override
  public Viewport createViewport(String clientId, ViewportDefinition viewportDefinition, AnalyticsListener listener) {
    return new TestViewport(viewportDefinition);
  }

  private static class TestViewport implements Viewport {

    private final Map<String, Object> _latestData = new HashMap<String, Object>();

    public TestViewport(ViewportDefinition viewportDefinition) {
      for (ViewportRow row : viewportDefinition.getRows()) {
        int rowId = row.getRowId();
        Object[] rowData = s_dummyData[rowId];
        _latestData.put(Integer.toString(rowId), rowData);
      }
    }

    @Override
    public Map<String, Object> getGridStructure() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getLatestData() {
      return _latestData;
    }

    @Override
    public void setRunning(boolean run) {
      s_logger.info("setRunning(), run: {}", run);
    }

    @Override
    public void setConversionMode(ConversionMode mode) {
      s_logger.info("setConversionMode(), mode: {}", mode);
    }
  }
}
