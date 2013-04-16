/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.testng.annotations.Test;

import com.opengamma.integration.tool.portfolio.xml.FilesystemPortfolioSchemaLocator;
import com.opengamma.integration.tool.portfolio.xml.SchemaVersion;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FilesystemPortfolioSchemaLocatorTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLocationMustNotBeNull() {
    new FilesystemPortfolioSchemaLocator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLocationMustNotExist() {
    new FilesystemPortfolioSchemaLocator(new File("I don't exist"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLocationMustBeDirectory() throws IOException {
    new FilesystemPortfolioSchemaLocator(createTemporaryFile());
  }

  @Test
  public void testNoSchemaFoundInEmptyDirectory() throws IOException {
    FilesystemPortfolioSchemaLocator locator = new FilesystemPortfolioSchemaLocator(createEmptySchemaLocation());
    assertNull(locator.lookupSchema(new SchemaVersion("1.0")));
  }

  @Test
  public void testMatchingSchemaFoundInDirectory() throws IOException {
    FilesystemPortfolioSchemaLocator locator = new FilesystemPortfolioSchemaLocator(
        createSchemaLocationWithVersions("1.0", "1.1", "2.0"));
    assertNotNull(locator.lookupSchema(new SchemaVersion("1.0")));
    assertNotNull(locator.lookupSchema(new SchemaVersion("1.1")));
    assertNotNull(locator.lookupSchema(new SchemaVersion("2.0")));
  }

  @Test
  public void testNoSchemaFoundInDirectory() throws IOException {
    FilesystemPortfolioSchemaLocator locator = new FilesystemPortfolioSchemaLocator(
        createSchemaLocationWithVersions("1.0", "1.1", "2.0"));
    assertNull(locator.lookupSchema(new SchemaVersion("1.2")));
    assertNull(locator.lookupSchema(new SchemaVersion("2.1")));
    assertNull(locator.lookupSchema(new SchemaVersion("3.0")));
  }

  private File createSchemaLocationWithVersions(String... versions) throws IOException {
    File dir = Files.createTempDirectory("portfolio-schemas").toFile();
    dir.deleteOnExit();

    for (String version : versions) {
      File f = new File(dir, "portfolio-schema-" + version + ".xsd");
      f.createNewFile();

      FileWriter writer = new FileWriter(f);
      // Write a minimal schema to the file
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <xs:schema version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> </xs:schema>");
      writer.flush();
      writer.close();

      f.deleteOnExit();
    }
    return dir;
  }

  private File createEmptySchemaLocation() throws IOException {
    return createSchemaLocationWithVersions();
  }

  private File createTemporaryFile() throws IOException {

    File file = File.createTempFile("portfolio-schema", ".xsd");
    file.deleteOnExit();
    return file;
  }

}
