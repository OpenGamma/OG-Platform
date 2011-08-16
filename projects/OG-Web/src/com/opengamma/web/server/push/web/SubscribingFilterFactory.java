/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.subscription.SubscriptionManager;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates {@link EntitySubscriptionFilter}s which create subscriptions for push notifications for changes to entities
 * requested via the REST interface.  If a REST method parameters is annotated with {@link Subscribe} it will be
 * interpreted as a {@link UniqueId} and any changes to the entity will cause an update to be pushed over
 * the long-polling HTTP interface.  The annotated parameter must be a string that can be parsed by
 * {@link UniqueId#parse(String)} and must also have a {@link PathParam} annotation.
 *
 * TODO this class needs access to the beans from the Spring context so it can make subscriptions
 * TODO servlet context? it's in there
 */
public class SubscribingFilterFactory implements ResourceFilterFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(SubscribingFilterFactory.class);

  @Context
  private HttpContext _httpContext;

  @Context
  private ServletContext _servletContext;

  @Override
  public List<ResourceFilter> create(AbstractMethod abstractMethod) {
    SubscriptionManager subscriptionManager = getSubscriptionManager();
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

  // TODO can the subscription manager go in a field that's automatically populated from the Spring context?
  private SubscriptionManager getSubscriptionManager() {
    ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(_servletContext);
    return context.getBean(SubscriptionManager.class);
  }

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
    s_logger.debug("Creating subscribing filter for parameters {} on method {}.{}()",
                   new Object[]{uidParamNames, method.getDeclaringClass().getSimpleName(), method.getName()});
    if (!uidParamNames.isEmpty()) {
      return new EntitySubscriptionFilter(_httpContext, uidParamNames);
    } else {
      return null;
    }
  }

  private static ResourceFilter createMasterSubscriptionFilter(AbstractMethod abstractMethod) {
    SubscribeMaster annotation = abstractMethod.getAnnotation(SubscribeMaster.class);
    if (annotation != null) {
      MasterType[] masterTypes = annotation.value();
      if (masterTypes.length > 0) {
        return new MasterSubscriptionFilter(Arrays.asList(masterTypes));
      } else {
        s_logger.warn("@SubscribeMaster annotation found on {}.{}() with no masters specified",
                      abstractMethod.getMethod().getDeclaringClass().getSimpleName(),
                      abstractMethod.getMethod().getName());
      }
    }
    return null;
  }
}

