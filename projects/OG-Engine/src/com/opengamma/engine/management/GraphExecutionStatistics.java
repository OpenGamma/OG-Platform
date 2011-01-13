/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
  public long getProcessedGraphs() {
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
  public long getExecutedGraphs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedGraphs() : 0; 
  }

  @Override
  public long getExecutedNodes() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedNodes() : 0; 
  }

  @Override
  public long getExecutionTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutionTime() : 0;  
  }

  @Override
  public long getActualTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getActualTime() : 0;  
  }

  @Override
  public long getProcessedJobs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    if (graphExecutionStatistics != null) {
      return graphExecutionStatistics.getProcessedJobs();
    }
    return 0;
  }

  @Override
  public long getProcessedJobSize() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobSize() : 0;    
  }

  @Override
  public long getProcessedJobCycleCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobCycleCost() : 0;
  }

  @Override
  public long getProcessedJobDataCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobDataCost() : 0;
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
  public double getAverageGraphSize() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageGraphSize() : 0;
  }

  @Override
  public double getAverageExecutionTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageExecutionTime() : 0;
  }

  @Override
  public double getAverageActualTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageActualTime() : 0;
  }

  @Override
  public double getAverageJobSize() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageJobSize() : 0;
  }

  @Override
  public double getAverageJobCycleCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageJobCycleCost() : 0;
  }

  @Override
  public double getAverageJobDataCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getAverageJobDataCost() : 0;
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
