/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a {@link com.opengamma.engine.view.calc.stats.GraphExecutionStatistics}.
 */
public class GraphExecutionStatistics implements GraphExecutionStatisticsMBean {
  
  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;
  
  private final com.opengamma.engine.view.View _view;
  
  private final String _calcConfigName;

  private final ObjectName _objectName;
  
  /**
   * Create a management GraphExecutionStatistics
   * 
   * @param view the view
   * @param statisticsProvider the statistics provider
   * @param viewProcessorName the view processor name
   * @param calcConfigName the calculation configuration name
   */
  public GraphExecutionStatistics(com.opengamma.engine.view.View view, TotallingGraphStatisticsGathererProvider statisticsProvider, String viewProcessorName, String calcConfigName) {
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    ArgumentChecker.notNull(viewProcessorName, "ViewProcessor Name");
    ArgumentChecker.notNull(view, "View");
    ArgumentChecker.notNull(calcConfigName, "calcConfig Name");
    _objectName = createObjectName(viewProcessorName, view.getName(), calcConfigName);
    _view = view;
    _calcConfigName = calcConfigName;
    _statisticsProvider = statisticsProvider;
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=GraphExecutionStatistics,ViewProcessor=<viewProcessorName>,View=<viewName>,name=<calcConfigName>"
   */
  static ObjectName createObjectName(String viewProcessorName, String viewName, String calcConfigName) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=GraphExecutionStatistics,ViewProcessor=" + viewProcessorName + ",View=" + viewName + ",name=" + calcConfigName);
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }

  @Override
  public String getViewName() {
    return _view.getName();
  }

  @Override
  public String getCalcConfigName() {
    return _calcConfigName;
  }

  @Override
  public Long getProcessedGraphs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedGraphs() : 0; 
  }
  
  private com.opengamma.engine.view.calc.stats.GraphExecutionStatistics getGraphExecutionStatistics() {
    Statistics statisticsGatherer = _statisticsProvider.getStatisticsGatherer(_view);
    List<com.opengamma.engine.view.calc.stats.GraphExecutionStatistics> executionStatistics = statisticsGatherer.getExecutionStatistics();
    for (com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics : executionStatistics) {
      if (graphExecutionStatistics.getCalcConfigName().equals(_calcConfigName) && graphExecutionStatistics.getViewName().equals(_view.getName())) {
        return graphExecutionStatistics;
      }
    }
    return null;
  }

  @Override
  public Long getExecutedGraphs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedGraphs() : null; 
  }

  @Override
  public Long getExecutedNodes() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedNodes() : null; 
  }

  @Override
  public Long getExecutionTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutionTime() : null;  
  }

  @Override
  public Long getActualTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getActualTime() : 0;  
  }

  @Override
  public Long getProcessedJobs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobs() : null; 
  }

  @Override
  public Long getProcessedJobSize() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobSize() : null;    
  }

  @Override
  public Long getProcessedJobCycleCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobCycleCost() : null;
  }

  @Override
  public Long getProcessedJobDataCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobDataCost() : null;
  }

  @Override
  public String getLastProcessedTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastProcessedTime().toString() : null;
  }

  @Override
  public String getLastExecutedTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastExecutedTime().toString() : null;
  }

  @Override
  public void reset() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    if (graphExecutionStatistics != null) {
      graphExecutionStatistics.reset();
    }
    
  }
  
  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }

}
