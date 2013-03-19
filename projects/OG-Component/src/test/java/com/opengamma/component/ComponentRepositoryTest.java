/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ServletContextAware;
import org.testng.annotations.Test;

import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.test.TestGroup;

/**
 * Test component repository.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentRepositoryTest {

  private static final ComponentLogger LOGGER = ComponentLogger.Sink.INSTANCE;

  public void test_registerSimple() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockSimple.class, "test");
    MockSimple mock = new MockSimple();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockSimple.class).size());
    assertEquals(mock, repo.getInstances(MockSimple.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockSimple.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockSimple.class, repo.getTypeInfo(MockSimple.class).getType());
    assertEquals(info, repo.getTypeInfo(MockSimple.class).getInfo("test"));
    repo.start();
    repo.stop();
  }

  public void test_registerLifecycle() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    MockInterfaces mock = new MockInterfaces();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
    assertEquals(0, mock.starts);
    assertEquals(0, mock.stops);
    repo.start();
    assertEquals(1, mock.starts);
    assertEquals(0, mock.stops);
    repo.stop();
    assertEquals(1, mock.starts);
    assertEquals(1, mock.stops);
  }

  public void test_registerSCAware() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    MockInterfaces mock = new MockInterfaces();
    repo.registerComponent(info, mock);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
    assertEquals(0, mock.servletContexts);
    repo.setServletContext(new MockServletContext());
    assertEquals(1, mock.servletContexts);
  }

  public void test_registerInitializingBean() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    MockInterfaces mock = new MockInterfaces();
    assertEquals(0, mock.inits);
    repo.registerComponent(info, mock);
    assertEquals(1, mock.inits);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
  }

  public void test_registerFactoryBean() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockInterfaces.class, "test");
    MockFactory mock = new MockFactory();
    assertEquals(0, mock.inits);
    assertEquals(0, mock.created.inits);
    repo.registerComponent(info, mock);
    assertEquals(1, mock.inits);
    assertEquals(1, mock.created.inits);
    assertEquals(1, repo.getInstanceMap().size());
    assertEquals(mock.created, repo.getInstanceMap().get(info.toComponentKey()));
    assertEquals(1, repo.getInstances(MockInterfaces.class).size());
    assertEquals(mock.created, repo.getInstances(MockInterfaces.class).iterator().next());
    assertEquals(1, repo.getTypeInfo().size());
    assertEquals(MockInterfaces.class, repo.getTypeInfo().iterator().next().getType());
    assertEquals(MockInterfaces.class, repo.getTypeInfo(MockInterfaces.class).getType());
    assertEquals(info, repo.getTypeInfo(MockInterfaces.class).getInfo("test"));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void test_registerAfterStart() {
    ComponentRepository repo = new ComponentRepository(LOGGER);
    ComponentInfo info = new ComponentInfo(MockSimple.class, "test");
    repo.registerComponent(info, new MockSimple());
    repo.start();
    repo.registerComponent(info, new MockSimple());
  }

  //-------------------------------------------------------------------------
  static class MockSimple {
  }

  static class MockInterfaces implements Lifecycle, ServletContextAware, InitializingBean {
    int starts;
    int stops;
    int servletContexts;
    int inits;
    @Override
    public void start() {
      starts++;
    }
    @Override
    public void stop() {
      stops++;
    }
    @Override
    public boolean isRunning() {
      return false;
    }
    @Override
    public void setServletContext(ServletContext servletContext) {
      servletContexts++;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
      inits++;
    }
  }

  static class MockFactory extends SingletonFactoryBean<MockInterfaces> implements Lifecycle {
    int starts;
    int stops;
    int inits;
    MockInterfaces created = new MockInterfaces();
    @Override
    public void start() {
      starts++;
    }
    @Override
    public void stop() {
      stops++;
    }
    @Override
    public boolean isRunning() {
      return false;
    }
    @Override
    public void afterPropertiesSet() {
      inits++;
      super.afterPropertiesSet();
    }
    @Override
    protected MockInterfaces createObject() {
      return created;
    }
  }

}
