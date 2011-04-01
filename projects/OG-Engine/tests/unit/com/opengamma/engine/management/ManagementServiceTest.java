/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;


import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;

/**
 * Tests the exposed MBeans and ManagementServiceTest can register MBeans
 */
@Test
public class ManagementServiceTest {
  
  private static final String ANOTHER_TEST_VIEW = "ANOTHER_TEST_VIEW";
  private static final Logger s_logger = LoggerFactory.getLogger(ManagementServiceTest.class);
  private static final int MBEANS_IN_TEST_VIEWPROCESSOR = 3;
  private MBeanServer _mBeanServer;
  private TotallingGraphStatisticsGathererProvider _statisticsProvider;
  private ViewProcessorTestEnvironment _env;

  /**
   * @throws java.lang.Exception
   */
  @BeforeMethod
  public void setUp() throws Exception {
    _env = new ViewProcessorTestEnvironment();
    _env.init();
    _mBeanServer = createMBeanServer();
    _statisticsProvider = new TotallingGraphStatisticsGathererProvider();
  }
  
  /**
   * @throws java.lang.Exception
   */
  @AfterMethod
  public void tearDown() throws Exception {
    ViewProcessorImpl viewProcessor = _env.getViewProcessor();
    if (viewProcessor.isRunning()) {
      viewProcessor.stop();
    }
    //Ensure the ViewProcessor stop clears all mbeans from the MBeanServer
    assertEquals(0, _mBeanServer.queryNames(new ObjectName("com.opengamma:*"), null).size());
  }

  private MBeanServer createMBeanServer() {
    return MBeanServerFactory.createMBeanServer("SimpleAgent");
  }
  
  public void testRegistrationService() throws Exception {
    ViewProcessorImpl vp = _env.getViewProcessor();
    vp.start();
    ManagementService.registerMBeans(vp, _statisticsProvider, _mBeanServer);
    assertEquals(MBEANS_IN_TEST_VIEWPROCESSOR, _mBeanServer.queryNames(new ObjectName("com.opengamma:*"), null).size());
  }
  
  public void testRegistrationServiceListensForViewAdded() throws Exception {
    ViewProcessorImpl viewProcessor = _env.getViewProcessor();
    viewProcessor.start();
    ManagementService.registerMBeans(viewProcessor, _statisticsProvider, _mBeanServer);
    assertEquals(MBEANS_IN_TEST_VIEWPROCESSOR, _mBeanServer.queryNames(new ObjectName("com.opengamma:*"), null).size());
    addAnotherView(viewProcessor);
    s_logger.debug("after adding new views");
    assertEquals(MBEANS_IN_TEST_VIEWPROCESSOR + 2, _mBeanServer.queryNames(new ObjectName("com.opengamma:*"), null).size());
    
  }

  private void addAnotherView(ViewProcessorImpl viewprocessor) {
    ViewDefinition anotherDefinition = new ViewDefinition(ANOTHER_TEST_VIEW, ViewProcessorTestEnvironment.TEST_USER);
    anotherDefinition.addViewCalculationConfiguration(_env.getViewDefinition().getCalculationConfiguration(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME));
    _env.getViewDefinitionRepository().addDefinition(anotherDefinition);
    ViewClient client = viewprocessor.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(ANOTHER_TEST_VIEW, ExecutionOptions.realTime(), false);
  }
  
  
  @SuppressWarnings("unused")
  private void dumpMBeans() {
    Set<ObjectName> registeredObjectNames = null;
    try {
      // ViewProcessor MBean
      registeredObjectNames = _mBeanServer.queryNames(ViewProcessor.createObjectName(_env.getViewProcessor()), null);
      // Other MBeans for this ViewProcessor
      registeredObjectNames.addAll(_mBeanServer.queryNames(new ObjectName("com.opengamma:*,ViewProcessor=" + _env.getViewProcessor().toString()), null));
    } catch (MalformedObjectNameException e) {
      // this should not happen
      s_logger.warn("Error querying MBeanServer. Error was " + e.getMessage(), e);
    }
    
    for (ObjectName objectName : registeredObjectNames) {
      s_logger.debug(objectName.toString());
    }
  }
  

}
