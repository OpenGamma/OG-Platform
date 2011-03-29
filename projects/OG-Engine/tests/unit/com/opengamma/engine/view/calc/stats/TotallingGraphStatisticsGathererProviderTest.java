/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc.stats;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.List;
import java.util.Set;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;

/**
 * 
 */
@Test
public class TotallingGraphStatisticsGathererProviderTest {
  
  private static class TestView implements View {
    
    private final String _name;
    
    public TestView (final String name) {
      _name = name;
    }

    @Override
    public void assertAccessToLiveDataRequirements(UserPrincipal user) {
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
    public LiveDataInjector getLiveDataOverrideInjector() {
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
    public Set<ValueRequirement> getRequiredLiveData() {
      return null;
    }

    @Override
    public boolean isLiveComputationRunning() {
      return false;
    }

    @Override
    public ViewClient createClient(UserPrincipal credentials) {
      return null;
    }

    @Override
    public Set<String> getAllSecurityTypes() {
      return null;
    }

    @Override
    public ViewClient getClient(UniqueIdentifier id) {
      return null;
    }

    @Override
    public void init() {
    }
  }
  
  private TotallingGraphStatisticsGathererProvider _provider = new TotallingGraphStatisticsGathererProvider();
  
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
            Assert.fail ();
          }
        }
      } else {
        Assert.fail ();
      }
    }
    assertEquals (7, mask);
  }
  
}