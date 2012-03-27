/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;

/**
 * Utility for generating a portfolio of securities.
 */
public abstract class AbstractPortfolioGeneratorTool {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractPortfolioGeneratorTool.class);

  /**
   * Default portfolio size used by sub-classes.
   */
  protected static final int PORTFOLIO_SIZE = 200;

  private Random _random;
  private SecurityPersister _securityPersister;
  private ToolContext _toolContext;
  private Class<? extends AbstractPortfolioGeneratorTool> _classContext;
  private AbstractPortfolioGeneratorTool _objectContext;

  public AbstractPortfolioGeneratorTool() {
    _classContext = getClass();
  }

  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    return new PortfolioGenerator(createPortfolioNodeGenerator(PORTFOLIO_SIZE), portfolioNameGenerator);
  }

  public Portfolio createPortfolio(final String portfolioName) {
    return createPortfolioGenerator(new StaticNameGenerator(portfolioName)).createPortfolio();
  }

  public PortfolioNodeGenerator createPortfolioNodeGenerator(int portfolioSize) {
    throw new UnsupportedOperationException();
  }
  
  public PortfolioNode createPortfolioNode(final int size) {
    return createPortfolioNodeGenerator(size).createPortfolioNode();
  }

  public PortfolioNode createPortfolioNode() {
    return createPortfolioNode(PORTFOLIO_SIZE);
  }

  public Random getRandom() {
    return _random;
  }

  public void setRandom(final Random random) {
    _random = random;
  }

  private void setContext(final Class<? extends AbstractPortfolioGeneratorTool> classContext, final AbstractPortfolioGeneratorTool objectContext) {
    _classContext = classContext;
    _objectContext = objectContext;
  }

  private Class<? extends AbstractPortfolioGeneratorTool> getClassContext() {
    return _classContext;
  }

  private AbstractPortfolioGeneratorTool getObjectContext() {
    return _objectContext;
  }

  public SecurityPersister getSecurityPersister() {
    return _securityPersister;
  }

  public void setSecurityPersister(final SecurityPersister securityPersister) {
    _securityPersister = securityPersister;
  }

  public ToolContext getToolContext() {
    return _toolContext;
  }

  public void setToolContext(final ToolContext toolContext) {
    _toolContext = toolContext;
  }

  protected final void configure(final SecurityGenerator<?> securityGenerator) {
    if (getRandom() != null) {
      securityGenerator.setRandom(getRandom());
    }
    if (getToolContext() != null) {
      securityGenerator.setConfigSource(getToolContext().getConfigSource());
      securityGenerator.setConventionSource(getToolContext().getConventionBundleSource());
      securityGenerator.setHolidaySource(getToolContext().getHolidaySource());
      securityGenerator.setHistoricalSource(getToolContext().getHistoricalTimeSeriesSource());
      securityGenerator.setExchangeMaster(getToolContext().getExchangeMaster());
      securityGenerator.setRegionSource(getToolContext().getRegionSource());
      securityGenerator.setSecurityMaster(getToolContext().getSecurityMaster());
    }
    configureChain(securityGenerator);
  }

  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    if (getObjectContext() != null) {
      getObjectContext().configureChain(securityGenerator);
    }
  }

  protected void configure(final AbstractPortfolioGeneratorTool tool) {
    if (getRandom() != null) {
      tool.setRandom(getRandom());
    }
    if (getToolContext() != null) {
      tool.setToolContext(getToolContext());
    }
    if (getSecurityPersister() != null) {
      tool.setSecurityPersister(getSecurityPersister());
    }
  }

  /**
   * Command line option for specifying the portfolio name to generate.
   */
  public static final String PORTFOLIO_OPT = "portfolio";
  /**
   * Command line option to specifying the asset class to generate the portfolio for.
   */
  public static final String SECURITY_OPT = "security";
  /**
   * Command line option to specify to write to the database masters.
   */
  public static final String WRITE_OPT = "write";

  private AbstractPortfolioGeneratorTool getInstance(final Class<?> clazz, final String security) {
    if (!AbstractPortfolioGeneratorTool.class.isAssignableFrom(clazz)) {
      throw new OpenGammaRuntimeException("Couldn't find generator tool class for " + security);
    }
    try {
      final String className;
      int i = security.indexOf('.');
      if (i < 0) {
        className = clazz.getPackage().getName() + "." + security + "PortfolioGeneratorTool";
      } else {
        className = security;
      }
      final Class<?> instanceClass;
      try {
        s_logger.debug("Trying class {}", className);
        instanceClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        return getInstance(clazz.getSuperclass(), security);
      }
      s_logger.info("Loading {}", className);
      final AbstractPortfolioGeneratorTool tool = (AbstractPortfolioGeneratorTool) instanceClass.newInstance();
      tool.setContext(getClassContext(), this);
      return tool;
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Couldn't create generator tool instance for " + security, e);
    }
  }

  protected AbstractPortfolioGeneratorTool getInstance(final String security) {
    return getInstance(getClassContext(), security);
  }

  public void run(final ToolContext context, final String portfolioName, final String security, final boolean write) {
    final AbstractPortfolioGeneratorTool instance = getInstance(security);
    instance.setToolContext(context);
    instance.setRandom(new SecureRandom());
    final SecuritySource securitySource;
    if (write) {
      s_logger.info("Creating database security writer");
      securitySource = context.getSecuritySource();
      instance.setSecurityPersister(new MasterSecurityPersister(context.getSecurityMaster()));
    } else {
      s_logger.info("Using dummy security writer");
      final MockSecurityPersister securityPersister = new MockSecurityPersister();
      instance.setSecurityPersister(securityPersister);
      securitySource = securityPersister.getMockSecuritySource();
    }
    s_logger.info("Creating portfolio {}", portfolioName);
    final Portfolio portfolio = instance.createPortfolio(portfolioName);
    if (write) {
      s_logger.info("Writing portfolio to the database");
      final ManageablePortfolio newPortfolio = new ManageablePortfolio(portfolio.getName());
      newPortfolio.setAttributes(portfolio.getAttributes());
      newPortfolio.setRootNode(createPortfolioNode(context.getPositionMaster(), portfolio.getRootNode()));
      final PortfolioSearchRequest request = new PortfolioSearchRequest();
      request.setDepth(0);
      request.setIncludePositions(false);
      request.setName(portfolio.getName());
      final PortfolioSearchResult result = context.getPortfolioMaster().search(request);
      PortfolioDocument document = result.getFirstDocument();
      if (document != null) {
        s_logger.info("Overwriting portfolio {}", document.getUniqueId());
        document.setPortfolio(newPortfolio);
        context.getPortfolioMaster().update(document);
      } else {
        document = new PortfolioDocument(newPortfolio);
        context.getPortfolioMaster().add(document);
      }
    } else {
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("Portfolio {}", portfolioName);
        writePortfolio(securitySource, portfolio.getRootNode(), "");
      }
    }
  }

  private ManageablePortfolioNode createPortfolioNode(final PositionMaster positionMaster, final PortfolioNode node) {
    final ManageablePortfolioNode newNode = new ManageablePortfolioNode(node.getName());
    for (PortfolioNode child : node.getChildNodes()) {
      newNode.addChildNode(createPortfolioNode(positionMaster, child));
    }
    for (Position position : node.getPositions()) {
      final ManageablePosition newPosition = new ManageablePosition();
      newPosition.setAttributes(position.getAttributes());
      newPosition.setQuantity(position.getQuantity());
      newPosition.setSecurityLink(new ManageableSecurityLink(position.getSecurityLink()));
      for (Trade trade : position.getTrades()) {
        newPosition.addTrade(new ManageableTrade(trade));
      }
      newNode.addPosition(positionMaster.add(new PositionDocument(newPosition)).getUniqueId());
    }
    return newNode;
  }

  private void writePortfolio(final SecuritySource securitySource, final PortfolioNode node, final String indent) {
    s_logger.debug("{}+{}", indent, node.getName());
    for (PortfolioNode childNode : node.getChildNodes()) {
      writePortfolio(securitySource, childNode, indent + "  ");
    }
    for (Position position : node.getPositions()) {
      final Security security = position.getSecurityLink().resolve(securitySource);
      s_logger.debug("{} {} x {}", new Object[] {indent, position.getQuantity(), security });
    }
  }

  private Option required(final Option option) {
    option.setRequired(true);
    return option;
  }

  public void createOptions(final Options options) {
    options.addOption(required(new Option("p", PORTFOLIO_OPT, true, "sets the name of the portfolio to create")));
    options.addOption(required(new Option("s", SECURITY_OPT, true, "selects the asset class to populate the portfolio with")));
    options.addOption(new Option("w", WRITE_OPT, false, "writes the portfolio and securities to the masters"));
  }

  public void run(final ToolContext context, final CommandLine commandLine) {
    run(context, commandLine.getOptionValue(PORTFOLIO_OPT), commandLine.getOptionValue(SECURITY_OPT), commandLine.hasOption(WRITE_OPT));
  }

}
