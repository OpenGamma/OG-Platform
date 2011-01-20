/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

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
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.event.ViewProcessorEventListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates exposed MBeans and register with MBeanServer
 */
public final class ManagementService implements ViewProcessorEventListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ManagementService.class);
  
  private final ViewProcessorImpl _viewProcessor;
  private final MBeanServer _mBeanServer;
  private final UserPrincipal _user;
  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;
  private final ConcurrentHashMap<String, Set<String>> _calcConfigByViewName = new ConcurrentHashMap<String, Set<String>>();

  /**
   * A constructor for a management service for a range of possible MBeans.
   * 
   * @param viewProcessor the view processor
   * @param user the user with permission to manage views
   * @param mBeanServer the MBeanServer to register MBeans to
   */
  private ManagementService(ViewProcessorImpl viewProcessor, UserPrincipal user, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(mBeanServer, "MBeanServer");
    ArgumentChecker.notNull(user, "User");
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    _viewProcessor = viewProcessor;
    _mBeanServer = mBeanServer;
    _user = user;
    _statisticsProvider = statisticsProvider;
  }

  /**
   * A convenience static method which creates a ManagementService and initialises it with the
   * supplied parameters.
   *
   * @param viewProcessor         the ViewProcessor to listen to
   * @param user                  the user with permission to manage views
   * @param statisticsProvider    the statistics provider
   * @param mBeanServer           the MBeanServer to register MBeans to
   */
  public static void registerMBeans(ViewProcessorImpl viewProcessor, UserPrincipal user, TotallingGraphStatisticsGathererProvider statisticsProvider, MBeanServer mBeanServer) {
    ManagementService registry = new ManagementService(viewProcessor, user, statisticsProvider, mBeanServer);
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
    initializeViews();
    initializeGraphExecutionStatistics();
  }

  private void initializeGraphExecutionStatistics() throws Exception {
    for (String viewName : _viewProcessor.getViewNames()) {
      View view = _viewProcessor.getView(viewName, _user);
      if (view != null) {
        Set<String> configurationNames = view.getDefinition().getAllCalculationConfigurationNames();
        _calcConfigByViewName.putIfAbsent(viewName, configurationNames);
        for (String calcConfigName : configurationNames) {
          com.opengamma.engine.management.GraphExecutionStatistics graphStatistics = new com.opengamma.engine.management.GraphExecutionStatistics(view, _statisticsProvider, _viewProcessor.toString(),
              calcConfigName);
          registerGraphStatistics(graphStatistics);
        }
      }
    }
  }

  private void initializeViewProcessor() throws Exception {
    com.opengamma.engine.management.ViewProcessor viewProcessor = new com.opengamma.engine.management.ViewProcessor(_viewProcessor);
    registerViewProcessor(viewProcessor);
  }

  private void initializeViews() throws Exception {
    for (String viewName : _viewProcessor.getViewNames()) {
      com.opengamma.engine.management.View view = new com.opengamma.engine.management.View(_viewProcessor.getView(viewName, _user), _viewProcessor);
      if (view != null) {
        registerView(view);
      }
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

  private void registerView(com.opengamma.engine.management.View view) throws Exception {
    try {
      _mBeanServer.registerMBean(view, view.getObjectName());
    } catch (InstanceAlreadyExistsException e) {
      _mBeanServer.unregisterMBean(view.getObjectName());
      _mBeanServer.registerMBean(view, view.getObjectName());
    }
  }

  /**
   * Called immediately after a View has been created.
   * 
   * @param viewName the name of the view
   */
  @Override
  public void notifyViewAdded(String viewName) {
    ViewInternal view = _viewProcessor.getView(viewName, _user);
    com.opengamma.engine.management.View viewManagement = new com.opengamma.engine.management.View(view, _viewProcessor);
    if (view != null) {
      try {
        registerView(viewManagement);
      } catch (Exception e) {
        s_logger.warn("Error registering view for management for " + viewManagement.getObjectName() + " . Error was " + e.getMessage(), e);
      }
      Set<String> configurationNames = view.getDefinition().getAllCalculationConfigurationNames();
      _calcConfigByViewName.putIfAbsent(viewName, configurationNames);
      for (String calcConfigName : configurationNames) {
        com.opengamma.engine.management.GraphExecutionStatistics graphStatistics = new com.opengamma.engine.management.GraphExecutionStatistics(view, _statisticsProvider, _viewProcessor.toString(),
            calcConfigName);
        try {
          registerGraphStatistics(graphStatistics);
        } catch (Exception e) {
          s_logger.warn("Error registering GraphExecutionStatistics for management for " + graphStatistics.getObjectName() + " . Error was " + e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Called immediately after a View has been removed. 
   *
   * @param viewName the name of the view
   */
  @Override
  public void notifyViewRemoved(String viewName) {
    ObjectName objectName = null;
    try {
      objectName = com.opengamma.engine.management.View.createObjectName(_viewProcessor.toString(), viewName);
      _mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      s_logger.warn("Error unregistering view for management for " + objectName + " . Error was " + e.getMessage(), e);
    }
    Set<String> configurationNames = _calcConfigByViewName.get(viewName);
    if (configurationNames != null) {
      for (String configName : configurationNames) {
        objectName = com.opengamma.engine.management.GraphExecutionStatistics.createObjectName(_viewProcessor.toString(), viewName, configName);
        try {
          _mBeanServer.unregisterMBean(objectName);
        } catch (Exception e) {
          s_logger.warn("Error unregistering view for GraphExecutionStatistics for " + objectName + " . Error was " + e.getMessage(), e);
        }
      }
    }
    _calcConfigByViewName.remove(viewName);
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
