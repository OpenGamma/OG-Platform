package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;

/**
 * Responsible for locating an xsd schema with a particular version number.
 */
public interface SchemaLocator {

  /**
   * Checks if a file with the expected name for the specified version can
   * be found by this location.
   *
   * @param version the version of the schema to check for
   * @return a schema with the required version if it exists, null otherwise
   */
  public File lookupSchema(SchemaVersion version);
}
