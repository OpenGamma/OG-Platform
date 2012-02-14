/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a {@link com.opengamma.engine.view.client.ViewClient}.
 */
public class ViewClient implements ViewClientMBean {

  /**
   * Underlying
   */
  private final com.opengamma.engine.view.client.ViewClient _viewClient;
  
  private final ObjectName _objectName;
  
  /**
   * Creates a management view client
   * 
   * @param viewClient  the underlying view client
   */
  public ViewClient(com.opengamma.engine.view.client.ViewClient viewClient) {
    ArgumentChecker.notNull(viewClient, "viewClient");
    _viewClient = viewClient;
    _objectName = createObjectName(viewClient.getViewProcessor().getName(), viewClient.getUniqueId());
  }
  
  /**
   * Creates an object name using the scheme "com.opengamma:type=ViewClient,ViewProcessor=<viewProcessorName>,name=<viewClientId>"
   */
  /*package*/ static ObjectName createObjectName(String viewProcessorName, UniqueId viewClientId) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=ViewClient,ViewProcessor=ViewProcessor " + viewProcessorName + ",name=ViewClient " + viewClientId.getValue());
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }
  
  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _viewClient.getUniqueId();
  }

  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }

  @Override
  public ViewClientState getState() {
    return _viewClient.getState();
  }

  @Override
  public boolean isAttached() {
    return _viewClient.isAttached();
  }

  //-------------------------------------------------------------------------
  @Override
  public void setUpdatePeriod(long periodMillis) {
    _viewClient.setUpdatePeriod(periodMillis);
  }

  @Override
  public void pause() {
    _viewClient.pause();
  }

  @Override
  public void resume() {
    _viewClient.resume();
  }

  @Override
  public boolean isCompleted() {
    return _viewClient.isCompleted();
  }

  @Override
  public boolean isResultAvailable() {
    return _viewClient.isResultAvailable();
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    return _viewClient.getLatestResult();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isViewCycleAccessSupported() {
    return _viewClient.isViewCycleAccessSupported();
  }

  @Override
  public void setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
    _viewClient.setViewCycleAccessSupported(isViewCycleAccessSupported);
  }

  //-------------------------------------------------------------------------
  @Override
  public void shutdown() {
    _viewClient.shutdown();
  }

}
