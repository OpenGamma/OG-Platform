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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessInternal;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.event.ViewProcessorEventListener;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates exposed MBeans and register with MBeanServer
 */
public final class ManagementService implements ViewProcessorEventListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ManagementService.class);
  
  private final ViewProcessorImpl _viewProcessor;
  private final MBeanServer _mBeanServer;
  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;
  private final ConcurrentHashMap<UniqueId, Set<String>> _calcConfigByViewProcessId = new ConcurrentHashMap<UniqueId, Set<String>>();

  /**
   * A constructor for a management service for a range of possible MBeans.
   * 
   * @param viewProcessor the view processor
   * @param mBeanServer the MBeanServer to register MBeans to
   */
  private ManagementService(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(mBeanServer, "MBeanServer");
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    _viewProcessor = viewProcessor;
    _mBeanServer = mBeanServer;
    _statisticsProvider = statisticsProvider;
  }

  /**
   * A convenience static method which creates a ManagementService and initialises it with the
   * supplied parameters.
   *
   * @param viewProcessor         the ViewProcessor to listen to
   * @param statisticsProvider    the statistics provider
   * @param mBeanServer           the MBeanServer to register MBeans to
   */
  public static void registerMBeans(ViewProcessorImpl viewProcessor, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ManagementService registry = new ManagementService(viewProcessor, statisticsProvider, mBeanServer);
    registry.init();
  }

  /**
   * Call to register the mbeans in the mbean server and start and do any other required initialisation.
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
        com.opengamma.engine.management.GraphExecutionStatistics graphStatistics =
          new com.opengamma.engine.management.GraphExecutionStatistics(viewProcess, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
        registerGraphStatistics(graphStatistics);
      }
    }
  }

  private void initializeViewProcessor() throws Exception {
    com.opengamma.engine.management.ViewProcessor viewProcessor = new com.opengamma.engine.management.ViewProcessor(_viewProcessor);
    registerViewProcessor(viewProcessor);
  }

  private void initializeViewProcesses() throws Exception {
    for (ViewProcessInternal viewProcess : _viewProcessor.getViewProcesses()) {
      com.opengamma.engine.management.ViewProcess viewProcessBean = new com.opengamma.engine.management.ViewProcess(viewProcess, _viewProcessor);
      registerViewProcess(viewProcessBean);
    }
  }
  
  private void initializeViewClients() throws Exception {
    for (ViewClient viewClient : _viewProcessor.getViewClients()) {
      com.opengamma.engine.management.ViewClient viewClientBean = new com.opengamma.engine.management.ViewClient(viewClient);
      registerViewClient(viewClientBean);
    }
  }

  private void registerGraphStatistics(com.opengamma.engine.management.GraphExecutionStatistics graphStatistics) throws Exception {
    try {
      _mBeanServer.registerMBean(graphStatistics, graphStatistics.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(graphStatistics.getObjectName());
      _mBeanServer.registerMBean(graphStatistics, graphStatistics.getObjectName());
    }
  }

  private void registerViewProcessor(com.opengamma.engine.management.ViewProcessor viewProcessor) throws Exception {
    try {
      _mBeanServer.registerMBean(viewProcessor, viewProcessor.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(viewProcessor.getObjectName());
      _mBeanServer.registerMBean(viewProcessor, viewProcessor.getObjectName());
    }
    
  }

  private void registerViewProcess(com.opengamma.engine.management.ViewProcess view) throws Exception {
    try {
      _mBeanServer.registerMBean(view, view.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(view.getObjectName());
      _mBeanServer.registerMBean(view, view.getObjectName());
    }
  }
  
  private void registerViewClient(com.opengamma.engine.management.ViewClient viewClient) throws Exception {
    try {
      _mBeanServer.registerMBean(viewClient, viewClient.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(viewClient.getObjectName());
      _mBeanServer.registerMBean(viewClient, viewClient.getObjectName());
    }
  }

  @Override
  public void notifyViewProcessAdded(UniqueId viewProcessId) {
    ViewProcessInternal view = _viewProcessor.getViewProcess(viewProcessId);
    if (view == null) {
      return;
    }
    com.opengamma.engine.management.ViewProcess viewManagement = new com.opengamma.engine.management.ViewProcess(view, _viewProcessor);
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
      com.opengamma.engine.management.GraphExecutionStatistics graphStatistics =
        new com.opengamma.engine.management.GraphExecutionStatistics(view, _statisticsProvider, _viewProcessor.getName(), calcConfigName);
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
      objectName = com.opengamma.engine.management.ViewProcess.createObjectName(_viewProcessor.getName(), viewProcessId);
      _mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      s_logger.warn("Error unregistering view for management for " + objectName + " . Error was " + e.getMessage(), e);
    }
    Set<String> configurationNames = _calcConfigByViewProcessId.get(viewProcessId);
    if (configurationNames != null) {
      //String viewDefinitionName = _viewProcessor.getViewProcess(viewProcessId).getDefinitionName();
      for (String configName : configurationNames) {
        objectName = com.opengamma.engine.management.GraphExecutionStatistics.createObjectName(_viewProcessor.getName(), viewProcessId, configName);
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
    com.opengamma.engine.management.ViewClient viewClientBean = new com.opengamma.engine.management.ViewClient(viewClient);
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
      objectName = com.opengamma.engine.management.ViewClient.createObjectName(_viewProcessor.getName(), viewClientId);
      _mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      s_logger.warn("Error unregistering view client for management for "  + objectName + ". Error was " + e.getMessage(), e);
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
      registeredObjectNames = _mBeanServer.queryNames(ViewProcessor.createObjectName(_viewProcessor), null);
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
