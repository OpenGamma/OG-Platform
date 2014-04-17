/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SourceLinkResolverTest {

  @BeforeMethod
  public void setup() {
    // Ensure we don't have a thread local service context which could be used accidentally
    ThreadLocalServiceContext.init(null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void noThreadLocalContextGivesError() {

    SourceLinkResolver<Object, String, ConfigSource> resolver = createSourceLinkResolver();
    resolver.resolve("id");
  }

  public void threadLocalContextGetsUsed() {

    ServiceContext serviceContext = createContext(ConfigSource.class, VersionCorrectionProvider.class);

    ThreadLocalServiceContext.init(serviceContext);
    SourceLinkResolver<Object, String, ConfigSource> resolver = createSourceLinkResolver();

    resolver.resolve("id");
  }

  private ServiceContext createContext(Class<?>... services) {

    Map<Class<?>, Object> serviceMap = new HashMap<>();
    for (Class<?> aClass : services) {
      serviceMap.put(aClass, mock(aClass));
    }
    return ServiceContext.of(serviceMap);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noVersionCorrectionGivesError() {

    ServiceContext serviceContext = createContext(ConfigSource.class);
    SourceLinkResolver<Object, String, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve("id");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSourceGivesError() {

    ServiceContext serviceContext = createContext(VersionCorrectionProvider.class);
    SourceLinkResolver<Object, String, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve("id");
  }

  private SourceLinkResolver<Object, String, ConfigSource> createSourceLinkResolver() {
    return new SourceLinkResolver<Object, String, ConfigSource>() {
        @Override
        protected Class<ConfigSource> getSourceClass() {
          return ConfigSource.class;
        }

        @Override
        protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
          return vcProvider.getConfigVersionCorrection();
        }

        @Override
        protected Object executeQuery(ConfigSource source, String identifier, VersionCorrection versionCorrection) {
          return source.getLatestByName(Object.class, identifier);
        }

        @Override
        public LinkResolver<Object, String> withTargetType(Class<Object> targetType) {
          return this;
        }
    };
  }

  private SourceLinkResolver<Object, String, ConfigSource> createSourceLinkResolver(final ServiceContext serviceContext) {
    return new SourceLinkResolver<Object, String, ConfigSource>(serviceContext) {
        @Override
        protected Class<ConfigSource> getSourceClass() {
          return ConfigSource.class;
        }

        @Override
        protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
          return vcProvider.getConfigVersionCorrection();
        }

        @Override
        protected Object executeQuery(ConfigSource source, String identifier, VersionCorrection versionCorrection) {
          return source.getLatestByName(Object.class, identifier);
        }

        @Override
        public LinkResolver<Object, String> withTargetType(Class<Object> targetType) {
          return this;
        }
    };
  }

}
