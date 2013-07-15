/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.opengamma.OpenGammaRuntimeException;
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
   *
   * @param version the version of the schema to check for
   * @return a schema with the required version if it exists, null otherwise
   */
  @Override
  public Schema lookupSchema(SchemaVersion version) {

    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    File requiredFile = new File(_schemaLocation, "portfolio-schema-" + version.toString() + ".xsd");

    try {
      return requiredFile.exists() ? factory.newSchema(requiredFile) : null;
    } catch (SAXException e) {
      throw new OpenGammaRuntimeException("Unable to parse schema file", e);
    }
  }
}
