/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.push.ConnectionManager;
import com.opengamma.web.analytics.push.WebPushServletContextUtils;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * Creates {@link EntitySubscriptionFilter}s which create subscriptions for push notifications for changes to entities
 * requested via the REST interface.  If a REST method parameter is annotated with {@link Subscribe} it will be
 * interpreted as a {@link UniqueId} and any changes to the entity will cause an update to be pushed over
 * the long-polling HTTP interface.  The annotated parameter must be a string that can be parsed by
 * {@link UniqueId#parse(String)} and must also have a {@link PathParam} annotation.
 */
public class SubscribingFilterFactory implements ResourceFilterFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(SubscribingFilterFactory.class);

  /** HTTP context injected by Jersey.  This is a proxy that always points to the context for the current request */
  @Context
  private HttpContext _httpContext;

  /** Servlet context injected by Jersey.  This is a proxy that always points to the context for the current request */
  @Context
  private ServletContext _servletContext;

  /** Request injected by Jersey.  This is a proxy that always points to the current request */
  @Context
  private HttpServletRequest _servletRequest;

  @Override
  public List<ResourceFilter> create(AbstractMethod abstractMethod) {
    
    if (!WebPushServletContextUtils.isConnectionManagerAvailable(_servletContext)) {
      return Collections.emptyList();
    }
    
    List<ResourceFilter> filters = new ArrayList<ResourceFilter>();
    ResourceFilter entityFilter = createEntitySubscriptionFilter(abstractMethod);
    if (entityFilter != null) {
      filters.add(entityFilter);
    }
    ResourceFilter masterFilter = createMasterSubscriptionFilter(abstractMethod);
    if (masterFilter != null) {
      filters.add(masterFilter);
    }
    return filters;
  }

  private ConnectionManager getUpdateManager() {
    return WebPushServletContextUtils.getConnectionManager(_servletContext);
  }

  /**
   * Creates a filter that creates a subscription for an entity when the method is invoked.  The method must have a
   * parameter annotated with {@link Subscribe} and {@link PathParam} which is a string that can be parsed by
   * {@link UniqueId#parse(String)}.  A notification is sent when the object with the specified {@link UniqueId}
   * changes.
   * @param abstractMethod A Jersey REST method
   * @return A filter to set up subscriptions when the method is invoked or null if the method doesn't
   * need entity subscriptions
   */
  private ResourceFilter createEntitySubscriptionFilter(AbstractMethod abstractMethod) {
    Method method = abstractMethod.getMethod();
    Annotation[][] annotations = method.getParameterAnnotations();
    List<String> uidParamNames = new ArrayList<String>();
    // find params annotated with @Subscribe.  must also have @PathParam
    for (Annotation[] paramAnnotations : annotations) {
      boolean subscribe = false;
      String paramName = null;
      for (Annotation annotation : paramAnnotations) {
        if (annotation instanceof Subscribe) {
          subscribe = true;
        } else if (annotation instanceof PathParam) {
          paramName = ((PathParam) annotation).value();
        }
      }
      if (subscribe) {
        if (paramName != null) {
          uidParamNames.add(paramName);
        } else {
          s_logger.warn("@Subscribe annotation found without matching @PathParam on method {}.{}(), no subscription " +
                            "will be created", method.getDeclaringClass().getSimpleName(), method.getName());
        }
      }
    }
    if (!uidParamNames.isEmpty()) {
      s_logger.debug("Creating subscribing filter for parameters {} on method {}.{}()",
                     new Object[]{uidParamNames, method.getDeclaringClass().getSimpleName(), method.getName()});
      return new EntitySubscriptionFilter(uidParamNames, getUpdateManager(), _httpContext, _servletRequest);
    } else {
      return null;
    }
  }

  /**
   * Creates a filter that creates a subscription for a master when the method is invoked.  The method must be
   * annotated with {@link SubscribeMaster}.  A notification is sent when any data in the master changes.
   * @param abstractMethod A Jersey REST method
   * @return A filter to set up subscriptions when the method is invoked or null if the method doesn't
   * need master subscriptions
   */
  private ResourceFilter createMasterSubscriptionFilter(AbstractMethod abstractMethod) {
    SubscribeMaster annotation = abstractMethod.getAnnotation(SubscribeMaster.class);
    if (annotation != null) {
      MasterType[] masterTypes = annotation.value();
      if (masterTypes.length > 0) {
        return new MasterSubscriptionFilter(getUpdateManager(), Arrays.asList(masterTypes), _httpContext, _servletRequest);
      } else {
        s_logger.warn("@SubscribeMaster annotation found on {}.{}() with no masters specified",
                      abstractMethod.getMethod().getDeclaringClass().getSimpleName(),
                      abstractMethod.getMethod().getName());
      }
    }
    return null;
  }
}

