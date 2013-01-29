package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

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
