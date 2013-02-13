package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import com.opengamma.OpenGammaRuntimeException;
import com.sun.xml.internal.bind.IDResolver;

public abstract class PortfolioConversion {

  private final Class _portfolioDocumentClass;
  private final PfConverter _portfolioConverter;
  private final IdRefResolverFactory _idRefResolverFactory;

  public PortfolioConversion(Class portfolioDocumentClass,
                             PfConverter converter,
                             IdRefResolverFactory idRefResolverFactory) {

    _portfolioDocumentClass = portfolioDocumentClass;
    _portfolioConverter = converter;
    _idRefResolverFactory = idRefResolverFactory;
  }

  public VersionedPortfolioHandler convertPortfolio(File file) {

    try {
      return _portfolioConverter.convert(createUnmarshaller().unmarshal(file));
    } catch (JAXBException e) {
      throw new OpenGammaRuntimeException("Error parsing XML content", e);
    }
  }

  private Unmarshaller createUnmarshaller() throws JAXBException {

    JAXBContext jc = JAXBContext.newInstance(_portfolioDocumentClass);
    Unmarshaller unmarshaller = jc.createUnmarshaller();

    // Output parsing info to System.out
    unmarshaller.setEventHandler(new DefaultValidationEventHandler());

    // The resolver allows us to differentiate between trades and positions
    // that have the same id. With this a trade and position can both have
    // id = 1 in the xml file, yet be resolved correctly based on context.
    // TODO can this be done without using a sun.internal class?
    unmarshaller.setProperty(IDResolver.class.getName(), _idRefResolverFactory.create());
    return unmarshaller;
  }
}
