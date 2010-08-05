/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.util.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.xml.sax.InputSource;

/**
 * Extend from this to verify that a Spring configuration is valid. This is to spot
 * changes made to the code that prevent the beans from being instantiated properly. 
 */
public abstract class AbstractSpringContextValidationTest {

  private final GenericApplicationContext _springContext;

  protected AbstractSpringContextValidationTest() {
    _springContext = new GenericApplicationContext();
  }

  protected GenericApplicationContext getSpringContext() {
    return _springContext;
  }

  protected void loadClassPathResource(final String name) {
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    xmlReader.loadBeanDefinitions(new ClassPathResource(name));
  }

  protected void fileSystemResource(final String path) {
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    xmlReader.loadBeanDefinitions(new FileSystemResource(path));
  }

  protected void loadXMLResource(final String xml) {
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(getSpringContext());
    xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
    xmlReader.loadBeanDefinitions(new InputSource(new StringReader(xml)));
  }

  @Before
  public void runBefore() {
    getSpringContext().refresh();
  }

  @Test
  public void testSomethingHappened() {
    final String[] beans = getSpringContext().getBeanDefinitionNames();
    assertNotNull(beans);
    if (beans.length == 0) {
      fail("No beans created");
    }
    System.out.println("Beans created");
    for (String bean : beans) {
      System.out.println("\t" + bean);
    }
  }

  @After
  public void runAfter() {
    getSpringContext().close();
  }

  @SuppressWarnings("unchecked")
  protected <T> T assertBeanExists (final Class<T> clazz, final String name) {
    final Object bean = getSpringContext ().getBean(name);
    assertNotNull (bean);
    assertTrue(clazz.isAssignableFrom(bean.getClass()));
    return (T)bean;
  }

}
