/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.sheet.reader.SheetReader;

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
   * Constructs the reader from the supplied location. The following validation
   * steps are performed:
   * <ul>
   * <li>Check the file is valid xml and contains a schemaVersion attribute</li>
   * <li>Check that a converter is available for the schema version</li>
   * <li>Check the file validates against the required schema</li>
   * <li>Check the portfolio, position, trade, security details are valid</li>
   * </ul>
   *
   * @param inputStream the input stream of the file, must not be null
   * @param schemaRegister the schema register, use to determine what xml
   * versions can be handled
   */
  public XmlFileReader(InputStream inputStream, SchemaRegister schemaRegister) {

    // Create a ByteArrayInputStream to ensure reset() is available
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      org.apache.commons.io.IOUtils.copy(inputStream, baos);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Unable to read xml", e);
    }

    byte[] bytes = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

    SchemaVersion version = extractSchemaVersion(bais);
    PortfolioConversion converter = schemaRegister.getConverterForSchema(version);

    //reset after schema version is extracted
    bais.reset();

    if (converter != null) {
      Iterable<VersionedPortfolioHandler> handlers = converter.convertPortfolio(bais);
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

  private SchemaVersion extractSchemaVersion(InputStream inputStream) {
    return new SchemaVersionParser(new InputStreamReader(inputStream)).parseSchemaVersion();
  }

}
