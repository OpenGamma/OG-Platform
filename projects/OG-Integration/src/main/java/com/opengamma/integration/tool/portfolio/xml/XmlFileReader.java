/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;

/**
 * Parses an XML file and if the file is valid, generates a collection
 * of Portfolio readers (one for each portfolio in the file). Note that
 * as the class implements Iterable, it is stateful and not thread safe.
 */
public class XmlFileReader implements Iterable<PortfolioReader> {

  /**
   * The portfolio readers available after the file has been successfully parsed.
   */
  private final Iterable<PortfolioReader> _readers;

  /**
   * Constructs the reader from the supplied location. The following validation steps are performed:
   * <ul>
   *   <li>Check the file is valid xml and contains a schemaVersion attribute</li>
   *   <li>Check that a converter is available for the schema version</li>
   *   <li>Check the file validates against the required schema</li>
   *   <li>Check the portfolio, position, trade, security details are valid</li>
   * </ul>
   *
   * @param fileLocation the location of the file to load, must not be null
   * @param schemaRegister the schema register, use to determine what xml versions can be handled
   */
  public XmlFileReader(String fileLocation, SchemaRegister schemaRegister) {

    SchemaVersion version = extractSchemaVersion(fileLocation);

    PortfolioConversion converter = schemaRegister.getConverterForSchema(version);

    if (converter != null) {

      Iterable<VersionedPortfolioHandler> handlers =  converter.convertPortfolio(new File(fileLocation));
      _readers = Iterables.transform(handlers, new Function<VersionedPortfolioHandler, PortfolioReader>() {
        @Override
        public PortfolioReader apply(final VersionedPortfolioHandler vph) {
          return new XmlPortfolioReader(vph);
        }
      });
    } else {
      throw new OpenGammaRuntimeException("Unable to process schema version: " + version + " as no converter is available");
    }
  }

  @Override
  public Iterator<PortfolioReader> iterator() {
    return _readers.iterator();
  }

  private SchemaVersion extractSchemaVersion(String fileLocation) {
    try {
      return new SchemaVersionParser(new FileReader(fileLocation)).parseSchemaVersion();
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Cannot find file: " + fileLocation, e);
    }
  }
}
