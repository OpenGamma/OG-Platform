/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.engine.view.calcnode.ViewProcessorQuery;
/**
 * Currently a placeholder.
 *
 * @author kirk
 */
public class FunctionExecutionContext extends AbstractFunctionContext {
  public HistoricalDataProvider getHistoricalDataProvider() {
    return (HistoricalDataProvider) get("historicalDataProvider");
  }
  
  public void setHistoricalDataProvider(HistoricalDataProvider historicalDataProvider) {
    put("historicalDataProvider", historicalDataProvider);
  }
  
  public ViewProcessorQuery getViewProcessorQuery() {
    return (ViewProcessorQuery) get("viewProcessorQuery");
  }
  
  public void setViewProcessorQuery(ViewProcessorQuery viewProcessorQuery) {
    put("viewProcessorQuery", viewProcessorQuery);
  }
}
