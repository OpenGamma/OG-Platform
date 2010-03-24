/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.id.IdentificationDomain;

/**
 * An implementation of {@link PositionMaster} which backs onto
 * CSV-formatted files in a specified directory.
 *
 * @author kirk
 */
public class CSVPositionMaster implements PositionMaster {
  private static final Logger s_logger = LoggerFactory.getLogger(CSVPositionMaster.class);
  private final File _baseDirectory;
  private final Map<String, File> _portfolioFilesByName = new TreeMap<String, File>();
  private final Map<DomainSpecificIdentifier, Position> _positionsByIdentityKey = new TreeMap<DomainSpecificIdentifier, Position>();
  private final Map<DomainSpecificIdentifier, PortfolioNode> _nodesByIdentityKey = new TreeMap<DomainSpecificIdentifier, PortfolioNode>();
  
  public CSVPositionMaster() {
    _baseDirectory = null;
  }
  
  public CSVPositionMaster(String baseDirectoryName) {
    this(new File(baseDirectoryName));
  }
  
  public CSVPositionMaster(File baseDirectory) {
    if(baseDirectory == null) {
      throw new NullPointerException("Base directory must be specified.");
    }
    if(!baseDirectory.exists()) {
      throw new IllegalArgumentException("Base directory " + baseDirectory + " must exist.");
    }
    if(!baseDirectory.isDirectory()) {
      throw new IllegalArgumentException("Base directory " + baseDirectory + " must be a directory.");
    }
    try {
      _baseDirectory = baseDirectory.getCanonicalFile();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Cannot get canonical file reference for base directory " + baseDirectory, e);
    }
    populatePortfolioNames();
  }

  /**
   * 
   */
  protected void populatePortfolioNames() {
    File[] filesInBaseDirectory = getBaseDirectory().listFiles();
    for(File f : filesInBaseDirectory) {
      if(!f.isFile()) {
        continue;
      }
      if(f.isHidden()) {
        continue;
      }
      String fileName = f.getName();
      String portfolioName = getPortfolioName(fileName);
      _portfolioFilesByName.put(portfolioName, f);
    }
  }

  protected static String getPortfolioName(String fileName) {
    if(fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
      return fileName.substring(0, fileName.length() - 4);
    }
    return fileName;
  }

  /**
   * @return the baseDirectory
   */
  public File getBaseDirectory() {
    return _baseDirectory;
  }

  @Override
  public Portfolio getRootPortfolio(String portfolioName) {
    if(!_portfolioFilesByName.containsKey(portfolioName)) {
      return null;
    }
    Portfolio portfolio = loadPortfolio(portfolioName, _portfolioFilesByName.get(portfolioName));
    return portfolio;
  }

  @Override
  public Collection<String> getRootPortfolioNames() {
    return Collections.unmodifiableSet(_portfolioFilesByName.keySet());
  }
  
  protected Portfolio loadPortfolio(String portfolioName, File portfolioFile) {
    if(portfolioName == null) {
      throw new NullPointerException("Portfolio name must be specified.");
    }
    if(portfolioFile == null) {
      throw new NullPointerException("Portfolio file must be specified.");
    }
    if((!portfolioFile.isFile()) || (!portfolioFile.canRead())) {
      throw new IllegalArgumentException("Portfolio file " + portfolioFile + " is not a readable file.");
    }
    try {
      FileInputStream fis = new FileInputStream(portfolioFile);
      return loadPortfolio(portfolioName, fis);
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Unable to parse portfolio file " + portfolioFile, ioe);
    }
  }
  
  public Portfolio loadPortfolio(String portfolioName, InputStream portfolioStream) throws IOException {
    int currPosition = 0;
    PortfolioImpl portfolio = new PortfolioImpl(portfolioName);
    portfolio.setIdentityKey(portfolioName);
    _nodesByIdentityKey.put(portfolio.getIdentityKey(), portfolio);
    
    BufferedReader br = new BufferedReader(new InputStreamReader(portfolioStream));
    String line = null;
    while((line = br.readLine()) != null) {
      currPosition++;
      PositionBean position = parseLine(line);
      if(position != null) {
        String identityKey = portfolioName + "-" + currPosition;
        position.setIdentityKey(identityKey);
        portfolio.addPosition(position);
        _positionsByIdentityKey.put(position.getIdentityKey(), position);
      }
    }
    s_logger.info("{} parsed stream with {} positions", portfolioName, portfolio.getPositions().size());
    return portfolio;
  }

  /**
   * @param line
   * @return
   */
  protected static PositionBean parseLine(String line) {
    if(line == null) {
      return null;
    }
    String[] tokens = line.split(Pattern.quote(","));
    if(tokens.length < 3) {
      return null;
    }
    // First token is the quantity
    BigDecimal quantity = new BigDecimal(tokens[0].trim());
    
    // Each set of 2 tokens is then security id domain and then id 
    List<DomainSpecificIdentifier> securityIdentifiers = new ArrayList<DomainSpecificIdentifier>();
    for(int i = 1; i < (tokens.length - 1); i++) {
      String idDomain = tokens[i].trim();
      String idValue = tokens[++i].trim();
      DomainSpecificIdentifier id = new DomainSpecificIdentifier(new IdentificationDomain(idDomain), idValue);
      securityIdentifiers.add(id);
    }
    DomainSpecificIdentifiers securityKey = new DomainSpecificIdentifiers(securityIdentifiers);
    s_logger.debug("Loaded position: {} in {}", quantity, securityKey);
    
    PositionBean position = new PositionBean(quantity, securityKey);
    return position;
  }

  @Override
  public Position getPosition(DomainSpecificIdentifier identityKey) {
    return _positionsByIdentityKey.get(identityKey);
  }

  @Override
  public PortfolioNode getPortfolioNode(DomainSpecificIdentifier identityKey) {
    return _nodesByIdentityKey.get(identityKey);
  }
  
}
