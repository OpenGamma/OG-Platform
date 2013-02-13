package com.opengamma.integration.tool.portfolio.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

public class XmlPortfolioReader implements PortfolioReader {

  private final Iterator<ObjectsPair<ManageablePosition, ManageableSecurity[]>> _positionIterator;
  private final String _portfolioName;

  public XmlPortfolioReader(String fileLocation, SchemaRegister schemaRegister) {

    SchemaVersion version = extractSchemaVersion(fileLocation);

    PortfolioConversion converter = schemaRegister.getConverterForSchema(version);

    if (converter != null) {

      VersionedPortfolioHandler vph =  converter.convertPortfolio(new File(fileLocation));

      _portfolioName = vph.getPortfolioName();
      _positionIterator = vph.getPositionIterator();

    } else {
      throw new OpenGammaRuntimeException("Unable to process schema version: " + version);
    }
  }

  private SchemaVersion extractSchemaVersion(String fileLocation) {
    try {
      return new SchemaVersionParser(new FileReader(fileLocation)).parseSchemaVersion();
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Cannot find file: " + fileLocation, e);
    }
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    return _positionIterator.hasNext() ? _positionIterator.next() : null;
  }

  @Override
  public String[] getCurrentPath() {
    return new String[0];
  }

  @Override
  public void close() {

  }

  @Override
  public String getPortfolioName() {
    return _portfolioName;
  }
}
