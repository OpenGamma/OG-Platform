/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for constructing a portfolio node from child portfolio node generators.
 */
public class TreePortfolioNodeGenerator implements PortfolioNodeGenerator {

  private final NameGenerator _nameGenerator;
  private final List<PortfolioNodeGenerator> _childNodes;

  /**
   * Creates a new portfolio node generator.
   * 
   * @param nameGenerator the source of portfolio node names
   */
  public TreePortfolioNodeGenerator(final NameGenerator nameGenerator) {
    ArgumentChecker.notNull(nameGenerator, "nameGenerator");
    _nameGenerator = nameGenerator;
    _childNodes = new ArrayList<PortfolioNodeGenerator>();
  }

  public void addChildNode(final PortfolioNodeGenerator childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    _childNodes.add(childNode);
  }

  protected NameGenerator getNameGenerator() {
    return _nameGenerator;
  }

  @Override
  public PortfolioNode createPortfolioNode() {
    final SimplePortfolioNode node = new SimplePortfolioNode(getNameGenerator().createName());
    for (PortfolioNodeGenerator childNodeGenerator : _childNodes) {
      final PortfolioNode childNode = childNodeGenerator.createPortfolioNode();
      if (childNode != null) {
        node.addChildNode(childNode);
      }
    }
    return node;
  }

}
