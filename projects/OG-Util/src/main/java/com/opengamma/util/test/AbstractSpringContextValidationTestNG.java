/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.io.StringReader;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.xml.sax.InputSource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Extend from this to verify that a Spring configuration is valid. This is to spot
 * changes made to the code that prevent the beans from being instantiated properly. 
 */
public abstract class AbstractSpringContextValidationTestNG {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractSpringContextValidationTestNG.class);
  private ThreadLocal<GenericApplicationContext> _springContext = new ThreadLocal<GenericApplicationContext>();

  @DataProvider(name = "runModes")
  public static Object[][] data_runMode() {  // CSIGNORE
    return new Object[][] {
      {"shareddev"},
      {"standalone"},
    };
  }

  protected GenericApplicationContext getSpringContext() {
    return _springContext.get();
  }

  private GenericApplicationContext createSpringContext() {
    GenericApplicationContext springContext = new GenericApplicationContext();
    _springContext.set(springContext);
    return springContext;
  }

  //-------------------------------------------------------------------------
  /**
   * This should be called by the subclass to initialize the test.
   * 
   * @param configXml  the Spring XML file, not null
   */
  protected void loadClassPathResource(final String configXml) {
    GenericApplicationContext springContext = createSpringContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(springContext);
    xmlReader.loadBeanDefinitions(new ClassPathResource(configXml));
    springContext.refresh();
  }

  protected void loadFileSystemResource(final String path) {
    GenericApplicationContext springContext = createSpringContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    xmlReader.loadBeanDefinitions(new FileSystemResource(path));
    springContext.refresh();
  }

  protected void loadXMLResource(final String xml) {
    GenericApplicationContext springContext = createSpringContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
    xmlReader.loadBeanDefinitions(new InputSource(new StringReader(xml)));
    springContext.refresh();
  }

  protected void loadUrlResource(final String url) {
    try {
      GenericApplicationContext springContext = createSpringContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
      xmlReader.loadBeanDefinitions(new UrlResource(url));
      springContext.refresh();
      
    } catch (MalformedURLException ex) {
      throw new OpenGammaRuntimeException("Malformed URL - " + url, ex);
    }
  }

  /**
   * Populates the Spring context from multiple XML configuration files.
   * The file paths must have a prefix to indicate what kind of resource
   * they are, e.g. {@code file:} or {@code classpath:}.
   * 
   * @param filePaths  the file paths, not null
   */
  protected void loadResources(final String... filePaths) {
    GenericApplicationContext springContext = createSpringContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    for (String path : filePaths) {
      xmlReader.loadBeanDefinitions(path);
    }
    springContext.refresh();
  }

  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
  }

  //-------------------------------------------------------------------------
  /**
   * This tests that something was loaded.
   */
  protected void assertContextLoaded() {
    final String[] beans = getSpringContext().getBeanDefinitionNames();
    assertNotNull(beans);
    if (beans.length == 0) {
      fail("No beans created");
    }
    s_logger.info("{} beans created by {}", beans.length, getClass());
    for (String bean : beans) {
      s_logger.debug("Bean name {}", bean);
    }
  }

  /**
   * This tests that a specific bean was loaded.
   * 
   * @param <T> the bean type
   * @param clazz  the bean class, not null
   * @param name  the bean name, not null
   * @return the bean, not null
   */
  @SuppressWarnings("unchecked")
  protected <T> T assertBeanExists(final Class<T> clazz, final String name) {
    final Object bean = getSpringContext().getBean(name);
    assertNotNull(bean);
    assertEquals(true, clazz.isAssignableFrom(bean.getClass()));
    return (T) bean;
  }

//  protected void loadClassPathResource(final String name) {
//    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
//    xmlReader.loadBeanDefinitions(new ClassPathResource(name));
//  }
//
//  protected void loadFileSystemResource(final String path) {
//    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
//    xmlReader.loadBeanDefinitions(new FileSystemResource(path));
//  }
//
//  protected void loadXMLResource(final String xml) {
//    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
//    xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
//    xmlReader.loadBeanDefinitions(new InputSource(new StringReader(xml)));
//  }
//
//  protected void loadUrlResource(final String url) {
//    try {
//      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
//      xmlReader.loadBeanDefinitions(new UrlResource(url));
//    } catch (MalformedURLException ex) {
//      throw new OpenGammaRuntimeException("Malformed URL - " + url, ex);
//    }
//  }
//
//  protected void assertSomethingHappened() {
//    final String[] beans = getSpringContext().getBeanDefinitionNames();
//    assertNotNull(beans);
//    if (beans.length == 0) {
//      fail("No beans created");
//    }
//    System.out.println("Beans created");
//    for (String bean : beans) {
//      System.out.println("\t" + bean);
//    }
//  }
//
//  @SuppressWarnings("unchecked")
//  protected <T> T assertBeanExists (final Class<T> clazz, final String name) {
//    final Object bean = getSpringContext ().getBean(name);
//    assertNotNull (bean);
//    assertTrue(clazz.isAssignableFrom(bean.getClass()));
//    return (T)bean;
//  }

}
