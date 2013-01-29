package com.opengamma.integration.tool.portfolio;

import java.io.File;

import com.opengamma.util.ArgumentChecker;

/**
 * Checks a known directory to see if a portfolio schema with the required
 * version exists inside it.
 */
public class FilesystemPortfolioSchemaLocator implements SchemaLocator {

  private final File _schemaLocation;

  public FilesystemPortfolioSchemaLocator(File schemaLocation) {

    ArgumentChecker.notNull(schemaLocation, "schemaLocation");
    ArgumentChecker.isTrue(schemaLocation.exists(), "File {} could not be found", schemaLocation);
    ArgumentChecker.isTrue(schemaLocation.isDirectory(), "File {} is not a directory", schemaLocation);
    _schemaLocation = schemaLocation;
  }

  /**
   * Checks if a file with the expected name for the specified version exists in the
   * schema directory. If it does, then it is returned, else null.
   *
   * @param version the version of the schema to check for
   * @return a schema with the required version if it exists, null otherwise
   */
  @Override
  public File lookupSchema(SchemaVersion version) {

    File requiredFile = new File(_schemaLocation, "portfolio-schema-" + version.toString() + ".xsd");
    return requiredFile.exists() ? requiredFile : null;
  }
}
