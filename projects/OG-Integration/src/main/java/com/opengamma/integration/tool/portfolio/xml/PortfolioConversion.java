/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

public abstract class PortfolioConversion {

  public static final String SCHEMA_LOCATION = "portfolio-schemas";

  private final Class _portfolioDocumentClass;
  private final PortfolioDocumentConverter _portfolioConverter;
  private final IdRefResolverFactory _idRefResolverFactory;
  private final FilesystemPortfolioSchemaLocator _schemaLocator = new FilesystemPortfolioSchemaLocator(new File(getClass().getClassLoader().getResource(SCHEMA_LOCATION).getFile()));
  private final Schema _schema;

  public PortfolioConversion(SchemaVersion schemaVersion,
                             Class portfolioDocumentClass,
                             PortfolioDocumentConverter converter,
                             IdRefResolverFactory idRefResolverFactory) {

    _portfolioDocumentClass = portfolioDocumentClass;
    _portfolioConverter = converter;
    _idRefResolverFactory = idRefResolverFactory;
    _schema = _schemaLocator.lookupSchema(schemaVersion);

    ArgumentChecker.notNull(_schema, "schema");
  }

  public Iterable<VersionedPortfolioHandler> convertPortfolio(File file) {

    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      return _portfolioConverter.convert(unmarshaller.unmarshal(file));
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
