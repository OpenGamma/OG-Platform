/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.sass;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.opengamma.util.sass.JRubySassCompiler;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.WebResourceTestUtils;

/**
 * Tests {@code JRubySassCompiler}
 */
@Test(groups = TestGroup.UNIT)
public class JRubySassCompilerTest {

  private static JRubySassCompiler s_compiler;
  private static File s_sassDir;
    
  private static File NAVBAR_SCSS;
  private static File NAVBAR_CSS;
  
  private static File FAKESHADOW_SCSS;
  private static File FAKESHADOW_CSS;
  
  private static File VARIABLES_SCSS;
  private static File VARIABLES_CSS;
  
  @BeforeClass 
  public void staticInit() throws Exception {
    @SuppressWarnings("unused")
    Class<?> clazz = JRubySassCompilerTest.class;
    
    NAVBAR_SCSS = new File(getClass().getResource("navbar.scss").toURI());
    NAVBAR_CSS = new File(getClass().getResource("navbar.css").toURI());
    
    FAKESHADOW_SCSS = new File(getClass().getResource("fakeshadow.scss").toURI());
    FAKESHADOW_CSS = new File(getClass().getResource("fakeshadow.css").toURI());
    
    VARIABLES_SCSS = new File(getClass().getResource("variables.scss").toURI());
    VARIABLES_CSS = new File(getClass().getResource("variables.css").toURI());
    
    s_sassDir = WebResourceTestUtils.extractRubySassGem();
    s_compiler = new JRubySassCompiler(ImmutableList.of(s_sassDir.getPath()));
  }
  
  @AfterClass 
  public void cleanUp() {
    if (s_sassDir != null) {
      FileUtils.deleteQuietly(s_sassDir);
    }
  }
    
  public void compileSassString() throws Exception {
    
    final String input = Files.toString(NAVBAR_SCSS, Charset.defaultCharset());
    final String output = s_compiler.sassConvert(input);
    
    assertEquals(Files.toString(NAVBAR_CSS, Charset.defaultCharset()), output);
  }
  
  public void updateStyleSheets() throws Exception {
    final File templateDir = Files.createTempDir();
    Files.copy(NAVBAR_SCSS, new File(templateDir, NAVBAR_SCSS.getName()));
    Files.copy(FAKESHADOW_SCSS, new File(templateDir, FAKESHADOW_SCSS.getName()));
    Files.copy(VARIABLES_SCSS, new File(templateDir, VARIABLES_SCSS.getName()));
        
    final File cssDir = Files.createTempDir();
    s_compiler.updateStyleSheets(templateDir, cssDir);
        
    File navbar = new File(cssDir, "navbar.css");
    assertTrue(navbar.exists());
    assertTrue(Files.equal(NAVBAR_CSS, navbar));
    
    File fakeshadow = new File(cssDir, "fakeshadow.css");
    assertTrue(fakeshadow.exists());
    assertTrue(Files.equal(FAKESHADOW_CSS, fakeshadow));
    
    File variables = new File(cssDir, "variables.css");
    assertTrue(variables.exists());
    assertTrue(Files.equal(VARIABLES_CSS, variables));
    
    FileUtils.deleteDirectory(templateDir);
    FileUtils.deleteDirectory(cssDir);
  }
}
