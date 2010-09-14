/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc.stats;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.stats.GraphExecutionStatistics;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * 
 */
public class TotallingGraphStatisticsGathererProviderTest {
  
  private static class TestView implements View {
    
    private final String _name;
    
    public TestView (final String name) {
      _name = name;
    }

    @Override
    public boolean addDeltaResultListener(DeltaComputationResultListener deltaListener) {
      return false;
    }

    @Override
    public boolean addResultListener(ComputationResultListener resultListener) {
      return false;
    }

    @Override
    public void assertAccessToLiveDataRequirements(UserPrincipal user) {
    }

    @Override
    public SingleComputationCycle createCycle(long valuationTime) {
      return null;
    }

    @Override
    public ViewDefinition getDefinition() {
      return null;
    }

    @Override
    public ViewComputationResultModel getLatestResult() {
      return null;
    }

    @Override
    public LiveDataInjector getLiveDataInjector() {
      return null;
    }

    @Override
    public String getName() {
      return _name;
    }

    @Override
    public Portfolio getPortfolio() {
      return null;
    }

    @Override
    public ViewProcessingContext getProcessingContext() {
      return null;
    }

    @Override
    public Set<ValueRequirement> getRequiredLiveData() {
      return null;
    }

    @Override
    public ViewEvaluationModel getViewEvaluationModel() {
      return null;
    }

    @Override
    public boolean isRunning() {
      return false;
    }

    @Override
    public void recalculationPerformed(ViewComputationResultModel result) {
    }

    @Override
    public boolean removeDeltaResultLister(DeltaComputationResultListener deltaListener) {
      return false;
    }

    @Override
    public boolean removeResultListener(ComputationResultListener resultListener) {
      return false;
    }

    @Override
    public void runOneCycle() {
    }

    @Override
    public void runOneCycle(long valuationTime) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
  }
  
  private TotallingGraphStatisticsGathererProvider _provider = new TotallingGraphStatisticsGathererProvider();
  
  @Test
  public void testBasicOperation () {
    final View testView1 = new TestView ("View 1");
    final View testView2 = new TestView ("View 2");
    _provider.getStatisticsGatherer(testView1).graphProcessed("Config 1", 10, 20.0, 30.0, 40.0);
    _provider.getStatisticsGatherer(testView1).graphExecuted("Config 1", 200, 300, 400);
    _provider.getStatisticsGatherer(testView2).graphProcessed("Config 1", 20, 40.0, 50.0, 60.0);
    _provider.getStatisticsGatherer(testView2).graphExecuted("Config 1", 400, 500, 600);
    _provider.getStatisticsGatherer(testView2).graphProcessed("Config 2", 20, 40.0, 50.0, 60.0);
    _provider.getStatisticsGatherer(testView2).graphExecuted("Config 2", 400, 500, 600);
    final List<Statistics> statsList = _provider.getViewStatistics ();
    assertNotNull (statsList);
    assertEquals (2, statsList.size ());
    int mask = 0;
    for (Statistics stats : statsList) {
      if (stats.getViewName ().equals ("View 1")) {
        mask |= 1;
        final List<GraphExecutionStatistics> graphStatsList = stats.getExecutionStatistics();
        assertEquals (1, graphStatsList.size ());
        assertEquals ("View 1", graphStatsList.get (0).getViewName ());
        assertEquals ("Config 1", graphStatsList.get (0).getCalcConfigName());
      } else if (stats.getViewName ().equals ("View 2")) {
        final List<GraphExecutionStatistics> graphStatsList = stats.getExecutionStatistics();
        assertEquals (2, graphStatsList.size ());
        for (GraphExecutionStatistics graphStats : graphStatsList) {
          if (graphStats.getCalcConfigName().equals ("Config 1")) {
            mask |= 2;
            assertEquals ("View 2", graphStats.getViewName ());
          } else if (graphStats.getCalcConfigName().equals ("Config 2")) {
            mask |= 4;
            assertEquals ("View 2", graphStats.getViewName ());
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