/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.event.ViewProcessorEventListener;
import com.opengamma.engine.view.impl.ViewProcessInternal;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates exposed MBeans and register with MBeanServer.
 */
public final class ManagementService implements ViewProcessorEventListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ManagementService.class);

  /**
   * The underlying view processor.
   */
  private final ViewProcessorImpl _viewProcessor;
  /**
   * The MBean server.
   */
  private final MBeanServer _mBeanServer;
  /**
   * The statistics gatherer.
   */
  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;
  /**
   * The config.
   */
  private final ConcurrentHashMap<UniqueId, Set<String>> _calcConfigByViewProcessId = new ConcurrentHashMap<>();

  /**
   * Should beans be categorized by view processor or not. If only one view processor is expected then setting this to false means the MBean hierarchy is simpler to navigate.
   */
  private final boolean _splitByViewProcessor;

  /**
   * Have the MBeans already been initialized for the view processor. Used to avoid trying to register on the notifyViewProcessorStarted method if registration has already happened through the
   * constructor.
   */
  private boolean _isInitialized;

  //-------------------------------------------------------------------------
  /**
   * A convenience static method which creates a ManagementService and initializes it with the supplied parameters.
   * 
   * @param viewProcessor the view processor, not null
   * @param statisticsProvider the statistics provider, not null
   * @param mBeanServer the MBeanServer to register MBeans to, not null
   * @deprecated add section containing JmxManagementServiceFactory to ini file instead
   */
  @Deprecated
  public static void registerMBeans(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ManagementService registry = new ManagementService(viewProcessor, statisticsProvider, mBeanServer, false);
    registry.init();
  }

  //-------------------------------------------------------------------------
  /**
   * A constructor for a management service for a range of possible MBeans.
   * 
   * @param viewProcessor the view processor, not null
   * @param statisticsProvider the statistics provider, not null
   * @param mBeanServer the MBeanServer to register MBeans to, not null
   * @param splitByViewProcessor if true, then classify registered beans by their view processor. Only required if more than one view processor will be running.
   */
  public ManagementService(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer, boolean splitByViewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(mBeanServer, "MBeanServer");
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    _viewProcessor = viewProcessor;
    _mBeanServer = mBeanServer;
    _statisticsProvider = statisticsProvider;
    _splitByViewProcessor = splitByViewProcessor;
  }

  //-------------------------------------------------------------------------
  /**
   * Call to register the mbeans in the mbean server and start and do any other required initialization.
   * 
   * @throws net.sf.ehcache.CacheException - all exceptions are wrapped in CacheException
   */
  public void init() {
    try {
      initializeAndRegisterMBeans();
      _viewProcessor.getViewProcessorEventListenerRegistry().registerListener(this);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("MBean registration error", e);
    }
  }

  /**
   * @throws Exception
   * @throws InstanceAlreadyExistsException
   * @throws MBeanRegistrationException
   * @throws NotCompliantMBeanException
   */
  private void initializeAndRegisterMBeans() throws Exception {

    if (!_isInitialized) {
      initializeViewProcessor();
      initializeViewProcesses();
      initializeViewClients();
      initializeGraphExecutionStatistics();
      _isInitialized = true;
    }
  }

  private void initializeGraphExecutionStatistics() throws Exception {
    for (ViewProcess viewProcess : _viewProcessor.getViewProcesses()) {
      Set<String> configurationNames = viewProcess.getLatestViewDefinition().getAllCalculationConfigurationNames();
      _calcConfigByViewProcessId.putIfAbsent(viewProcess.getUniqueId(), configurationNames);
      for (String calcConfigName : configurationNames) {
        GraphExecutionStatisticsMBeanImpl graphStatistics = new GraphExecutionStatisticsMBeanImpl(viewProcess, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
        registerGraphStatistics(graphStatistics);
      }
    }
  }

  private void initializeViewProcessor() throws Exception {
    ViewProcessorMBeanImpl viewProcessor = new ViewProcessorMBeanImpl(_viewProcessor, _splitByViewProcessor);
    registerViewProcessor(viewProcessor);
  }

  private void initializeViewProcesses() throws Exception {
    for (ViewProcessInternal viewProcess : _viewProcessor.getViewProcesses()) {
      ViewProcessMXBeanImpl viewProcessBean = new ViewProcessMXBeanImpl(viewProcess, _viewProcessor, _splitByViewProcessor);
      registerViewProcess(viewProcessBean);
    }
  }

  private void initializeViewClients() throws Exception {
    for (ViewClient viewClient : _viewProcessor.getViewClients()) {
      ViewClientMBeanImpl viewClientBean = new ViewClientMBeanImpl(viewClient, _splitByViewProcessor);
      registerViewClient(viewClientBean);
    }
  }

  //-------------------------------------------------------------------------
  private void registerGraphStatistics(GraphExecutionStatisticsMBeanImpl graphStatistics) throws Exception {
    ObjectName objectName = graphStatistics.getObjectName();
    StandardMBean mbean = new StandardMBean(graphStatistics, GraphExecutionStatisticsMBean.class);
    try {
      _mBeanServer.registerMBean(mbean, objectName);
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(objectName);
      _mBeanServer.registerMBean(mbean, objectName);
    }
  }

  private void registerViewProcessor(ViewProcessorMBeanImpl viewProcessor) throws Exception {
    ObjectName objectName = viewProcessor.getObjectName();
    StandardMBean mBean = new StandardMBean(viewProcessor, ViewProcessorMBean.class);
    try {
      _mBeanServer.registerMBean(mBean, objectName);
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(objectName);
      _mBeanServer.registerMBean(mBean, objectName);
    }
  }

  private void registerViewProcess(ViewProcessMXBeanImpl viewProcessBean) throws Exception {
    registerViewProcess(viewProcessBean, viewProcessBean.getObjectName());
  }

  private void registerViewProcess(ViewProcessMXBeanImpl viewProcessBean, ObjectName objectName) throws Exception {
    try {
      _mBeanServer.registerMBean(viewProcessBean, objectName);
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(objectName);
      _mBeanServer.registerMBean(viewProcessBean, viewProcessBean.getObjectName());
    }
  }

  private void registerViewClient(ViewClientMBeanImpl viewClient) throws Exception {
    ObjectName objectName = viewClient.getObjectName();
    StandardMBean mBean = new StandardMBean(viewClient, ViewClientMBean.class);
    try {
      _mBeanServer.registerMBean(mBean, objectName);
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(objectName);
      _mBeanServer.registerMBean(mBean, objectName);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void notifyViewProcessAdded(UniqueId viewProcessId) {
    ViewProcessInternal view = _viewProcessor.getViewProcess(viewProcessId);
    if (view == null) {
      return;
    }
    ViewProcessMXBeanImpl viewManagement = new ViewProcessMXBeanImpl(view, _viewProcessor, _splitByViewProcessor);
    try {
      registerViewProcess(viewManagement);
    } catch (Exception e) {
      s_logger.warn("Error registering view for management for " + viewManagement.getObjectName() + " . Error was " + e.getMessage(), e);
    }
    ViewDefinition definition;
    try {
      definition = view.getLatestViewDefinition();
    } catch (DataNotFoundException e) {
      definition = null;
      s_logger.error("View process {} does not have a valid view definition", viewProcessId);
    }
    Set<String> configurationNames = Collections.emptySet();
    if (definition != null) {
      configurationNames = definition.getAllCalculationConfigurationNames();
    }
    _calcConfigByViewProcessId.putIfAbsent(viewProcessId, configurationNames);
    for (String calcConfigName : configurationNames) {
      GraphExecutionStatisticsMBeanImpl graphStatistics = new GraphExecutionStatisticsMBeanImpl(view, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
      try {
        registerGraphStatistics(graphStatistics);
      } catch (Exception e) {
        s_logger.warn("Error registering GraphExecutionStatistics for management for " + graphStatistics.getObjectName() + " . Error was " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void notifyViewAutomaticallyStarted(UniqueId viewProcessId, String autoStartName) {
    ViewProcessInternal view = _viewProcessor.getViewProcess(viewProcessId);
    if (view == null) {
      return;
    }
    ViewProcessMXBeanImpl viewManagement = new ViewProcessMXBeanImpl(view, _viewProcessor, _splitByViewProcessor);
    try {
      String beanNamePrefix = _splitByViewProcessor ? "com.opengamma:type=ViewProcessors,ViewProcessor=ViewProcessor " + _viewProcessor.getName() : "com.opengamma:type=ViewProcessor";
      String beanName = beanNamePrefix + ",AutoStartViews=AutoStartViews,name=AutoStart [" + autoStartName + "]";
      registerViewProcess(viewManagement, new ObjectName(beanName));
    } catch (Exception e) {
      s_logger.warn("Error registering view for management for " + viewManagement.getObjectName() + " . Error was " + e.getMessage(), e);
    }
  }

  @Override
  public void notifyViewProcessRemoved(UniqueId viewProcessId) {
    ObjectName objectName = null;
    try {
      objectName = ViewProcessMXBeanImpl.createObjectName(_viewProcessor.getName(), viewProcessId, _splitByViewProcessor);
      _mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      s_logger.warn("Error unregistering view for management for " + objectName + " . Error was " + e.getMessage(), e);
    }
    Set<String> configurationNames = _calcConfigByViewProcessId.get(viewProcessId);
    if (configurationNames != null) {
      //String viewDefinitionName = _viewProcessor.getViewProcess(viewProcessId).getDefinitionName();
      for (String configName : configurationNames) {
        objectName = GraphExecutionStatisticsMBeanImpl.createObjectName(_viewProcessor.getName(), viewProcessId, configName);
        try {
          _mBeanServer.unregisterMBean(objectName);
        } catch (Exception e) {
          s_logger.warn("Error unregistering view for GraphExecutionStatistics for " + objectName + " . Error was " + e.getMessage(), e);
        }
      }
    }
    _calcConfigByViewProcessId.remove(viewProcessId);
  }

  @Override
  public void notifyViewClientAdded(UniqueId viewClientId) {
    ViewClient viewClient = _viewProcessor.getViewClient(viewClientId);
    ViewClientMBeanImpl viewClientBean = new ViewClientMBeanImpl(viewClient, _splitByViewProcessor);
    try {
      registerViewClient(viewClientBean);
    } catch (Exception e) {
      s_logger.warn("Error registering view client for management for " + viewClientBean.getObjectName() + ". Error was " + e.getMessage(), e);
    }
  }

  @Override
  public void notifyViewClientRemoved(UniqueId viewClientId) {
    ObjectName objectName = null;
    try {
      objectName = ViewClientMBeanImpl.createObjectName(_viewProcessor.getName(), viewClientId, _splitByViewProcessor);
      _mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      s_logger.warn("Error unregistering view client for management for " + objectName + ". Error was " + e.getMessage(), e);
    }
  }

  @Override
  public void notifyViewProcessorStarted() {
    try {
      initializeAndRegisterMBeans();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("MBean registration error", e);
    }
  }

  @Override
  public void notifyViewProcessorStopped() {
    Set<ObjectName> registeredObjectNames = null;
    try {
      // ViewProcessor MBean
      registeredObjectNames = _mBeanServer.queryNames(ViewProcessorMBeanImpl.createObjectName(_viewProcessor, _splitByViewProcessor), null);
      // Other MBeans for this ViewProcessor
      registeredObjectNames.addAll(_mBeanServer.queryNames(new ObjectName("com.opengamma:*,ViewProcessor=" + _viewProcessor.toString()), null));
    } catch (MalformedObjectNameException e) {
      // this should not happen
      s_logger.warn("Error querying MBeanServer. Error was " + e.getMessage(), e);
    }

    for (ObjectName objectName : registeredObjectNames) {
      try {
        _mBeanServer.unregisterMBean(objectName);
      } catch (Exception e) {
        s_logger.warn("Error unregistering object instance " + objectName + " . Error was " + e.getMessage(), e);
      }
    }

    _isInitialized = false;
  }

}
