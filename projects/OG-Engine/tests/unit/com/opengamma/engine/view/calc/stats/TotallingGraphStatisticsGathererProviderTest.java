/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class TotallingGraphStatisticsGathererProviderTest {
  
  private TotallingGraphStatisticsGathererProvider _provider = new TotallingGraphStatisticsGathererProvider();
  
  @Test
  public void testBasicOperation () {
    UniqueIdentifier vp1Id = UniqueIdentifier.of("Test", "ViewProcess1");
    UniqueIdentifier vp2Id = UniqueIdentifier.of("Test", "ViewProcess2");
    _provider.getStatisticsGatherer(vp1Id).graphProcessed("Config 1", 10, 20.0, 30.0, 40.0);
    _provider.getStatisticsGatherer(vp1Id).graphExecuted("Config 1", 200, 300, 400);
    _provider.getStatisticsGatherer(vp2Id).graphProcessed("Config 1", 20, 40.0, 50.0, 60.0);
    _provider.getStatisticsGatherer(vp2Id).graphExecuted("Config 1", 400, 500, 600);
    _provider.getStatisticsGatherer(vp2Id).graphProcessed("Config 2", 20, 40.0, 50.0, 60.0);
    _provider.getStatisticsGatherer(vp2Id).graphExecuted("Config 2", 400, 500, 600);
    final List<Statistics> statsList = _provider.getViewStatistics ();
    assertNotNull (statsList);
    assertEquals (2, statsList.size ());
    int mask = 0;
    for (Statistics stats : statsList) {
      if (stats.getViewProcessId().equals(vp1Id)) {
        mask |= 1;
        final List<GraphExecutionStatistics> graphStatsList = stats.getExecutionStatistics();
        assertEquals (1, graphStatsList.size());
        assertEquals (vp1Id, graphStatsList.get (0).getViewProcessId());
        assertEquals ("Config 1", graphStatsList.get (0).getCalcConfigName());
      } else if (stats.getViewProcessId().equals(vp2Id)) {
        final List<GraphExecutionStatistics> graphStatsList = stats.getExecutionStatistics();
        assertEquals (2, graphStatsList.size ());
        for (GraphExecutionStatistics graphStats : graphStatsList) {
          if (graphStats.getCalcConfigName().equals ("Config 1")) {
            mask |= 2;
            assertEquals (vp2Id, graphStats.getViewProcessId());
          } else if (graphStats.getCalcConfigName().equals ("Config 2")) {
            mask |= 4;
            assertEquals (vp2Id, graphStats.getViewProcessId());
          } else {
            fail ();
          }
        }
      } else {
        fail ();
      }
    }
    assertEquals (7, mask);
  }
  
}