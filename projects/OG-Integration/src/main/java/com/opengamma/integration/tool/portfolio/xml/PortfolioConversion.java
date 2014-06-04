/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Defines the schema-insensitive interface for converting a portfolio
 * document. The schema version is parsed and if available used to
 * parse the XML document.
 */
public abstract class PortfolioConversion {

  /**
   * The directory holding the schema.
   */
  public static final String SCHEMA_LOCATION = "portfolio-schemas";
  /**
   * The directory holding the schema.
   */
  public static final File SCHEMA_DIRECTORY;

  static {
    URL resource = PortfolioConversion.class.getClassLoader().getResource(SCHEMA_LOCATION);
    if (resource == null) {
      throw new OpenGammaRuntimeException("File not found in classpath: " + SCHEMA_LOCATION);
    }
    try {
      SCHEMA_DIRECTORY = new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new OpenGammaRuntimeException("File not found in classpath: " + SCHEMA_LOCATION);
    }
  }
  /**
   * The XML locator for the schema.
   */
  private static final FilesystemPortfolioSchemaLocator SCHEMA_LOCATOR =
      new FilesystemPortfolioSchemaLocator(SCHEMA_DIRECTORY);

  private final Class<?> _portfolioDocumentClass;
  private final PortfolioDocumentConverter<Object> _portfolioConverter;
  @SuppressWarnings("unused")
  private final IdRefResolverFactory _idRefResolverFactory;
  private final Schema _schema;

  /**
   * Creates an instance.
   * 
   * @param schemaVersion  the schema version, not null
   * @param portfolioDocumentClass  the portfolio class, not null
   * @param converter  the converter, not null
   * @param idRefResolverFactory  the resolver, not null
   */
  @SuppressWarnings("unchecked")
  public PortfolioConversion(SchemaVersion schemaVersion,
                             Class<?> portfolioDocumentClass,
                             PortfolioDocumentConverter<?> converter,
                             IdRefResolverFactory idRefResolverFactory) {

    _portfolioDocumentClass = portfolioDocumentClass;
    _portfolioConverter = (PortfolioDocumentConverter<Object>) converter;
    _idRefResolverFactory = idRefResolverFactory;
    _schema = SCHEMA_LOCATOR.lookupSchema(schemaVersion);

    ArgumentChecker.notNull(_schema, "schema");
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the portfolio.
   * 
   * @param inputStream  the inputStream to read, not null
   * @return the converted file, not null
   */
  public Iterable<VersionedPortfolioHandler> convertPortfolio(InputStream inputStream) {
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      return _portfolioConverter.convert(unmarshaller.unmarshal(inputStream));
    } catch (JAXBException e) {
      throw new OpenGammaRuntimeException("Error parsing XML content", e);
    }
  }

  private Unmarshaller createUnmarshaller() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(_portfolioDocumentClass);
    Unmarshaller unmarshaller = jc.createUnmarshaller();

    unmarshaller.setSchema(_schema);

    // Output parsing info to System.out
    unmarshaller.setEventHandler(new DefaultValidationEventHandler());

    // The resolver allows us to differentiate between trades and positions
    // that have the same id. With this a trade and position can both have
    // id = 1 in the xml file, yet be resolved correctly based on context.
    // TODO can this be done without using a sun.internal class?
    //unmarshaller.setProperty(IDResolver.class.getName(), _idRefResolverFactory.create());
    return unmarshaller;
  }

}
