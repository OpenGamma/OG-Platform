/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.id.UniqueId;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates {@link EntitySubscriptionFilter}s which create subscriptions for push notifications for changes to entities
 * requested via the REST interface.  If a REST method parameter is annotated with {@link Subscribe} it will be
 * interpreted as a {@link UniqueId} and any changes to the entity will cause an update to be pushed over
 * the long-polling HTTP interface.  The annotated parameter must be a string that can be parsed by
 * {@link UniqueId#parse(String)} and must also have a {@link PathParam} annotation.
 *
 * TODO this class needs access to the beans from the Spring context so it can make subscriptions
 */
public class SubscribingFilterFactory implements ResourceFilterFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(SubscribingFilterFactory.class);

  @Context
  private HttpContext _httpContext;

  @Context
  private ServletContext _servletContext;

  @Context
  private HttpServletRequest _servletRequest;

  @Override
  public List<ResourceFilter> create(AbstractMethod abstractMethod) {
    RestUpdateManager restUpdateManager = getUpdateManager();
    List<ResourceFilter> filters = new ArrayList<ResourceFilter>();
    ResourceFilter entityFilter = createEntitySubscriptionFilter(abstractMethod, restUpdateManager, _servletRequest);
    if (entityFilter != null) {
      filters.add(entityFilter);
    }
    ResourceFilter masterFilter = createMasterSubscriptionFilter(abstractMethod, restUpdateManager);
    if (masterFilter != null) {
      filters.add(masterFilter);
    }
    return filters;
  }

  private RestUpdateManager getUpdateManager() {
    ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(_servletContext);
    return context.getBean(RestUpdateManager.class);
  }

  private ResourceFilter createEntitySubscriptionFilter(AbstractMethod abstractMethod,
                                                        RestUpdateManager restUpdateManager,
                                                        HttpServletRequest servletRequest) {
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
      return new EntitySubscriptionFilter(_httpContext, uidParamNames, restUpdateManager, servletRequest);
    } else {
      return null;
    }
  }

  private static ResourceFilter createMasterSubscriptionFilter(AbstractMethod abstractMethod,
                                                               RestUpdateManager restUpdateManager) {
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

