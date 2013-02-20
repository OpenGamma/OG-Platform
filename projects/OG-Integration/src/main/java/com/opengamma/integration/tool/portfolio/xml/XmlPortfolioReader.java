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

  private final Iterator<PortfolioPosition> _positionIterator;
  private final String _portfolioName;
  private String[] _currentPath = new String[0];

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
    return _positionIterator.hasNext() ? processPosition(_positionIterator.next()) : null;
  }

  private ObjectsPair<ManageablePosition, ManageableSecurity[]> processPosition(PortfolioPosition position) {
    // Handle a portfolio level change
    _currentPath = position.getPortfolioPath();
    return new ObjectsPair<>(position.getPosition(), position.getSecurities());
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }

  @Override
  public void close() {

  }

  @Override
  public String getPortfolioName() {
    return _portfolioName;
  }
}
