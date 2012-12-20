/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for constructing a random, but reasonable, portfolio.
 */
public class PortfolioGenerator {

  private final PortfolioNodeGenerator _rootNodeGenerator;
  private final NameGenerator _nameGenerator;

  public PortfolioGenerator(final PortfolioNodeGenerator rootNodeGenerator, final NameGenerator nameGenerator) {
    ArgumentChecker.notNull(rootNodeGenerator, "rootNodeGenerator");
    ArgumentChecker.notNull(nameGenerator, "nameGenerator");
    _rootNodeGenerator = rootNodeGenerator;
    _nameGenerator = nameGenerator;
  }

  protected PortfolioNodeGenerator getRootNodeGenerator() {
    return _rootNodeGenerator;
  }

  protected NameGenerator getNameGenerator() {
    return _nameGenerator;
  }

  public Portfolio createPortfolio() {
    final PortfolioNode root = getRootNodeGenerator().createPortfolioNode();
    final SimplePortfolioNode rootNode;
    if (root instanceof SimplePortfolioNode) {
      rootNode = (SimplePortfolioNode) root;
    } else {
      rootNode = new SimplePortfolioNode(root);
    }
    return new SimplePortfolio(getNameGenerator().createName(), rootNode);
  }

}
