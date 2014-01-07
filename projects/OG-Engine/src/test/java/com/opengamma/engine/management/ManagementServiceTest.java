/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the exposed MBeans and ManagementServiceTest can register MBeans
 */
@Test(groups = TestGroup.INTEGRATION)
public class ManagementServiceTest {

  private static final String ANOTHER_TEST_VIEW = "ANOTHER_TEST_VIEW";
  private static final Logger s_logger = LoggerFactory.getLogger(ManagementServiceTest.class);
  private static final int MBEANS_IN_TEST_VIEWPROCESSOR = 1;
  private MBeanServer _mBeanServer;
  private TotallingGraphStatisticsGathererProvider _statisticsProvider;
  private ViewProcessorTestEnvironment _env;

  /**
   * @throws java.lang.Exception
   */
  @BeforeMethod
  public void setUp() throws Exception {
    TestLifecycle.begin();
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
    try {
      ViewProcessorImpl viewProcessor = _env.getViewProcessor();
      viewProcessor.stop();
      //Ensure the ViewProcessor stop clears all mbeans from the MBeanServer
      assertMBeanCount(0);
    } finally {
      TestLifecycle.end();
    }
  }

  private MBeanServer createMBeanServer() {
    return MBeanServerFactory.createMBeanServer("SimpleAgent");
  }

  public void testRegistrationService() throws Exception {
    ViewProcessorImpl vp = _env.getViewProcessor();
    vp.start();
    ManagementService.registerMBeans(vp, _statisticsProvider, _mBeanServer);
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR);
  }

  public void testRegistrationServiceListensForViewProcessAdded() throws Exception {
    ViewProcessorImpl viewProcessor = _env.getViewProcessor();
    viewProcessor.start();
    ManagementService.registerMBeans(viewProcessor, _statisticsProvider, _mBeanServer);
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR);
    addAnotherView(viewProcessor);
    s_logger.debug("after adding new views");
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR + 3);
  }

  public void testRegistrationServiceListenersForViewClientAdded() throws Exception {
    ViewProcessorImpl viewProcessor = _env.getViewProcessor();
    viewProcessor.start();
    ManagementService.registerMBeans(viewProcessor, _statisticsProvider, _mBeanServer);
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR);
    ViewClient client1 = viewProcessor.createViewClient(UserPrincipal.getTestUser());
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR + 1);
    ViewClient client2 = viewProcessor.createViewClient(UserPrincipal.getTestUser());
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR + 2);
    client1.shutdown();
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR + 1);
    client2.shutdown();
    assertMBeanCount(MBEANS_IN_TEST_VIEWPROCESSOR);
  }

  private void assertMBeanCount(int count) throws MalformedObjectNameException {
    assertEquals(count, _mBeanServer.queryNames(new ObjectName("com.opengamma:*"), null).size());
  }

  private void addAnotherView(ViewProcessorImpl viewprocessor) {
    ViewDefinition anotherDefinition = new ViewDefinition(UniqueId.of("boo", "far"), ANOTHER_TEST_VIEW, ViewProcessorTestEnvironment.TEST_USER);
    anotherDefinition.addViewCalculationConfiguration(_env.getViewDefinition().getCalculationConfiguration(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME));
    _env.getMockViewDefinitionRepository().put(anotherDefinition);
    ViewClient client = viewprocessor.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(anotherDefinition.getUniqueId(), ExecutionOptions.infinite(MarketData.live()), false);
  }

  @SuppressWarnings("unused")
  private void dumpMBeans() {
    Set<ObjectName> registeredObjectNames = null;
    try {
      // ViewProcessor MBean
      registeredObjectNames = _mBeanServer.queryNames(ViewProcessorMBeanImpl.createObjectName(_env.getViewProcessor(), true), null);
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
