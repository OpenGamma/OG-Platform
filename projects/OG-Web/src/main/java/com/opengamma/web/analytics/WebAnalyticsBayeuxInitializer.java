/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.cometd.Bayeux;

import com.opengamma.web.server.LiveResultsServiceBean;

/**
 * Listener that binds CometD and the live results service.
 */
public class WebAnalyticsBayeuxInitializer implements ServletContextAttributeListener {

  /**
   * The service bean.
   */
  private LiveResultsServiceBean _liveResultsServiceBean;
  /**
   * The bayeux instance.
   */
  private Bayeux _bayeux;

  @Override
  public void attributeAdded(ServletContextAttributeEvent event) {
    // spot attributes (created in any order) and bind them together
    if (Bayeux.ATTRIBUTE.equals(event.getName())) {
      _bayeux = (Bayeux) event.getValue();
      bind();
    } else if (WebAnalyticsResource.LIVE_RESULTS_SERVICE_ATTRIBUTE.equals(event.getName())) {
      _liveResultsServiceBean = (LiveResultsServiceBean) event.getValue();
      bind();
    }
  }

  /**
   * Binds the elements together when we have both.
   */
  protected void bind() {
    if (_bayeux != null && _liveResultsServiceBean != null) {
      _liveResultsServiceBean.init(_bayeux);
    }
  }

  @Override
  public void attributeRemoved(ServletContextAttributeEvent event) {
    // no action
  }

  @Override
  public void attributeReplaced(ServletContextAttributeEvent event) {
    // no action
  }

}
