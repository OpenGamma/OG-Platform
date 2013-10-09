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
  private final ConcurrentHashMap<UniqueId, Set<String>> _calcConfigByViewProcessId = new ConcurrentHashMap<UniqueId, Set<String>>();

  //-------------------------------------------------------------------------
  /**
   * A convenience static method which creates a ManagementService and
   * initializes it with the supplied parameters.
   *
   * @param viewProcessor  the view processor, not null
   * @param statisticsProvider  the statistics provider, not null
   * @param mBeanServer  the MBeanServer to register MBeans to, not null
   */
  public static void registerMBeans(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ManagementService registry = new ManagementService(viewProcessor, statisticsProvider, mBeanServer);
    registry.init();
  }

  //-------------------------------------------------------------------------
  /**
   * A constructor for a management service for a range of possible MBeans.
   * 
   * @param viewProcessor  the view processor, not null
   * @param statisticsProvider  the statistics provider, not null
   * @param mBeanServer  the MBeanServer to register MBeans to, not null
   */
  private ManagementService(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(mBeanServer, "MBeanServer");
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    _viewProcessor = viewProcessor;
    _mBeanServer = mBeanServer;
    _statisticsProvider = statisticsProvider;
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
    initializeViewProcessor();
    initializeViewProcesses();
    initializeViewClients();
    initializeGraphExecutionStatistics();
  }

  private void initializeGraphExecutionStatistics() throws Exception {
    for (ViewProcess viewProcess : _viewProcessor.getViewProcesses()) {
      Set<String> configurationNames = viewProcess.getLatestViewDefinition().getAllCalculationConfigurationNames();
      _calcConfigByViewProcessId.putIfAbsent(viewProcess.getUniqueId(), configurationNames);
      for (String calcConfigName : configurationNames) {
        GraphExecutionStatisticsMBeanImpl graphStatistics =
            new GraphExecutionStatisticsMBeanImpl(viewProcess, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
        registerGraphStatistics(graphStatistics);
      }
    }
  }

  private void initializeViewProcessor() throws Exception {
    ViewProcessorMBeanImpl viewProcessor = new ViewProcessorMBeanImpl(_viewProcessor);
    registerViewProcessor(viewProcessor);
  }

  private void initializeViewProcesses() throws Exception {
    for (ViewProcessInternal viewProcess : _viewProcessor.getViewProcesses()) {
      ViewProcessMXBeanImpl viewProcessBean = new ViewProcessMXBeanImpl(viewProcess, _viewProcessor);
      registerViewProcess(viewProcessBean);
    }
  }

  private void initializeViewClients() throws Exception {
    for (ViewClient viewClient : _viewProcessor.getViewClients()) {
      ViewClientMBeanImpl viewClientBean = new ViewClientMBeanImpl(viewClient);
      registerViewClient(viewClientBean);
    }
  }

  //-------------------------------------------------------------------------
  private void registerGraphStatistics(GraphExecutionStatisticsMBeanImpl graphStatistics) throws Exception {
    try {
      StandardMBean mbean = new StandardMBean(graphStatistics, GraphExecutionStatisticsMBean.class);
      _mBeanServer.registerMBean(mbean, graphStatistics.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(graphStatistics.getObjectName());
      _mBeanServer.registerMBean(graphStatistics, graphStatistics.getObjectName());
    }
  }

  private void registerViewProcessor(ViewProcessorMBeanImpl viewProcessor) throws Exception {
    try {
      StandardMBean mbean = new StandardMBean(viewProcessor, ViewProcessorMBean.class);
      _mBeanServer.registerMBean(mbean, viewProcessor.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(viewProcessor.getObjectName());
      _mBeanServer.registerMBean(viewProcessor, viewProcessor.getObjectName());
    }
  }

  private void registerViewProcess(ViewProcessMXBeanImpl viewProcessBean) throws Exception {
    try {
      StandardMBean mbean = new StandardMBean(viewProcessBean, ViewProcessMXBean.class);
      _mBeanServer.registerMBean(mbean, viewProcessBean.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(viewProcessBean.getObjectName());
      _mBeanServer.registerMBean(viewProcessBean, viewProcessBean.getObjectName());
    }
  }

  private void registerViewClient(ViewClientMBeanImpl viewClient) throws Exception {
    try {
      StandardMBean mbean = new StandardMBean(viewClient, ViewClientMBean.class);
      _mBeanServer.registerMBean(mbean, viewClient.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(viewClient.getObjectName());
      _mBeanServer.registerMBean(viewClient, viewClient.getObjectName());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void notifyViewProcessAdded(UniqueId viewProcessId) {
    ViewProcessInternal view = _viewProcessor.getViewProcess(viewProcessId);
    if (view == null) {
      return;
    }
    ViewProcessMXBeanImpl viewManagement = new ViewProcessMXBeanImpl(view, _viewProcessor);
    try {
      registerViewProcess(viewManagement);
    } catch (Exception e) {
      s_logger.warn("Error registering view for management for " + viewManagement.getObjectName() + " . Error was " + e.getMessage(), e);
    }
    ViewDefinition definition = view.getLatestViewDefinition();
    Set<String> configurationNames = Collections.emptySet();
    if (definition != null) {
      configurationNames = definition.getAllCalculationConfigurationNames();
    }
    _calcConfigByViewProcessId.putIfAbsent(viewProcessId, configurationNames);
    for (String calcConfigName : configurationNames) {
      GraphExecutionStatisticsMBeanImpl graphStatistics =
          new GraphExecutionStatisticsMBeanImpl(view, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
      try {
        registerGraphStatistics(graphStatistics);
      } catch (Exception e) {
        s_logger.warn("Error registering GraphExecutionStatistics for management for " + graphStatistics.getObjectName() + " . Error was " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void notifyViewProcessRemoved(UniqueId viewProcessId) {
    ObjectName objectName = null;
    try {
      objectName = ViewProcessMXBeanImpl.createObjectName(_viewProcessor.getName(), viewProcessId);
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
    ViewClientMBeanImpl viewClientBean = new ViewClientMBeanImpl(viewClient);
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
      objectName = ViewClientMBeanImpl.createObjectName(_viewProcessor.getName(), viewClientId);
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
      registeredObjectNames = _mBeanServer.queryNames(ViewProcessorMBeanImpl.createObjectName(_viewProcessor), null);
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
  }

}
