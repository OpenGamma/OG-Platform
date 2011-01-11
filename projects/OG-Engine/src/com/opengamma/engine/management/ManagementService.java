/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ManagementService {

  private final ViewProcessor _viewProcessor;
  private final MBeanServer _mBeanServer;
  private final UserPrincipal _user;

  /**
   * A constructor for a management service for a range of possible MBeans.
   * 
   * @param viewProcessor the view processor
   * @param user the user with permission to manage views
   * @param mBeanServer the MBeanServer to register MBeans to
   */
  public ManagementService(ViewProcessor viewProcessor, UserPrincipal user, MBeanServer mBeanServer) {
    ArgumentChecker.notNull(viewProcessor, "View Processor");
    ArgumentChecker.notNull(mBeanServer, "MBeanServer");
    ArgumentChecker.notNull(user, "User");
    _viewProcessor = viewProcessor;
    _mBeanServer = mBeanServer;
    _user = user;
  }

  /**
   * A convenience static method which creates a ManagementService and initialises it with the
   * supplied parameters.
   *
   * @param viewProcessor               the ViewProcessor to listen to
   * @param user the user with permission to manage views
   * @param mBeanServer                 the MBeanServer to register MBeans to
   */
  public static void registerMBeans(ViewProcessor viewProcessor, UserPrincipal user, MBeanServer mBeanServer) {
    ManagementService registry = new ManagementService(viewProcessor, user, mBeanServer);
    registry.init();
  }

  /**
   * Call to register the mbeans in the mbean server and start and do any other required initialisation.
   *
   * @throws net.sf.ehcache.CacheException - all exceptions are wrapped in CacheException
   */
  public void init() {
    com.opengamma.engine.management.ViewProcessor viewProcessor = new com.opengamma.engine.management.ViewProcessor(_viewProcessor);
    try {
      registerViewProcessor(viewProcessor);
      for (String viewName : _viewProcessor.getViewNames()) {
        com.opengamma.engine.management.View view = new com.opengamma.engine.management.View(_viewProcessor.getView(viewName, _user), _viewProcessor);
        registerView(view);
      }
      
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("MBean registration error", e);
    }
  }

  private void registerViewProcessor(com.opengamma.engine.management.ViewProcessor viewProcessor) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    _mBeanServer.registerMBean(viewProcessor, viewProcessor.getObjectName());
  }
  
  private void registerView(com.opengamma.engine.management.View view) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    _mBeanServer.registerMBean(view, view.getObjectName());
  }

}
