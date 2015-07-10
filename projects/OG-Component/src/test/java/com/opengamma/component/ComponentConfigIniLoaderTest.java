/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentConfigIniLoaderTest {

  private static final ComponentLogger LOGGER = ComponentLogger.Sink.INSTANCE;
  private static final String NEWLINE = "\n";

  public void test_loadValid() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String text =
        "# comment" + NEWLINE +
        "[global]" + NEWLINE +
        "a = c" + NEWLINE +
        "b = d" + NEWLINE +
        "" + NEWLINE +
        "[block]" + NEWLINE +
        "m = p" + NEWLINE +
        "n = ${a}" + NEWLINE +  // property from [global]
        "o = ${input}" + NEWLINE;  // property from injected properties
    Resource resource = new ByteArrayResource(text.getBytes(Charsets.UTF_8), "Test");
    properties.put("input", "text");
    
    ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(2, test.getGroups().size());
    
    ConfigProperties testGlobal = test.getGroup("global");
    assertEquals(2, testGlobal.size());
    assertEquals("c", testGlobal.getValue("a"));
    assertEquals("d", testGlobal.getValue("b"));
    
    ConfigProperties testBlock = test.getGroup("block");
    assertEquals(3, testBlock.size());
    assertEquals("p", testBlock.getValue("m"));
    assertEquals("c", testBlock.getValue("n"));
    assertEquals("text", testBlock.getValue("o"));
  }

  public void test_loadValid_emptyGlobal() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String text =
        "# comment" + NEWLINE +
        "[global]" + NEWLINE +
        "" + NEWLINE +
        "[block]" + NEWLINE +
        "m = p" + NEWLINE;
    Resource resource = new ByteArrayResource(text.getBytes(Charsets.UTF_8), "Test");
    
    ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(2, test.getGroups().size());
    
    ConfigProperties testGlobal = test.getGroup("global");
    assertEquals(0, testGlobal.size());
    
    ConfigProperties testBlock = test.getGroup("block");
    assertEquals(1, testBlock.size());
    assertEquals("p", testBlock.getValue("m"));
  }

  public void test_loadValid_groupPropertyOverride() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String text =
        "# comment" + NEWLINE +
        "[block]" + NEWLINE +
        "m = p" + NEWLINE;
    Resource resource = new ByteArrayResource(text.getBytes(Charsets.UTF_8), "Test");
    properties.put("[block].m", "override");
    
    ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(1, test.getGroups().size());
    
    ConfigProperties testBlock = test.getGroup("block");
    assertEquals(1, testBlock.size());
    assertEquals("override", testBlock.getValue("m"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = ComponentConfigException.class)
  public void test_loadInvalid_doubleKey() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String str =
        "[block]" + NEWLINE +
        "m = p" + NEWLINE +
        "m = s" + NEWLINE;
    Resource resource = new ByteArrayResource(str.getBytes());
    
    loader.load(resource, 0, new ComponentConfig());
  }

  @Test(expectedExceptions = ComponentConfigException.class)
  public void test_loadInvalid_replacementNotFound() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String str =
        "[block]" + NEWLINE +
        "m = ${notFound}" + NEWLINE;
    Resource resource = new ByteArrayResource(str.getBytes());
    
    loader.load(resource, 0, new ComponentConfig());
  }

  @Test(expectedExceptions = ComponentConfigException.class)
  public void test_loadInvalid_propertyNotInGroup() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String str =
        "m = foo" + NEWLINE;
    Resource resource = new ByteArrayResource(str.getBytes());
    
    loader.load(resource, 0, new ComponentConfig());
  }

  @Test(expectedExceptions = ComponentConfigException.class)
  public void test_loadInvalid_propertyNoEquals() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String str =
        "[block]" + NEWLINE +
        "m" + NEWLINE;
    Resource resource = new ByteArrayResource(str.getBytes());
    
    loader.load(resource, 0, new ComponentConfig());
  }

  @Test(expectedExceptions = ComponentConfigException.class)
  public void test_loadInvalid_propertyEmptyKey() {
    ConfigProperties properties = new ConfigProperties();
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties );
    String str =
        "[block]" + NEWLINE +
        "= foo" + NEWLINE;
    Resource resource = new ByteArrayResource(str.getBytes());
    
    loader.load(resource, 0, new ComponentConfig());
  }

}
