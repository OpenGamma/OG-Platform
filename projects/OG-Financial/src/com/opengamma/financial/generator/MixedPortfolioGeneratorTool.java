/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

/**
 * Utility for constructing a mixed portfolio containing all asset classes
 */
public class MixedPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  private static final String[] CLASSES = new String[] {"CapFloorCMSSpread", "CapFloor", "Cash", "FRA", "MixedFX", "Swap", "Swaption" };

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final int sizePerAsset = Math.max(size / CLASSES.length, 10);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Mixed"));
    for (String clazz : CLASSES) {
      final AbstractPortfolioGeneratorTool tool = getInstance(clazz);
      configure(tool);
      final PortfolioNodeGenerator node = tool.createPortfolioNodeGenerator(sizePerAsset);
      rootNode.addChildNode(node);
    }
    return rootNode;
  }

}
