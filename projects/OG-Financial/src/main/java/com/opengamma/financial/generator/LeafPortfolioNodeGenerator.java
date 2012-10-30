/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for constructing a portfolio node containing a number of positions in random, but reasonable, securities.
 */
public class LeafPortfolioNodeGenerator implements PortfolioNodeGenerator {

  private final NameGenerator _nameGenerator;
  private final PositionGenerator _positionGenerator;
  private final int _size;

  /**
   * Creates a new portfolio node generator.
   * 
   * @param nameGenerator the source of portfolio node names
   * @param positionGenerator the source of positions
   * @param size the number of positions to put under created nodes
   */
  public LeafPortfolioNodeGenerator(final NameGenerator nameGenerator, final PositionGenerator positionGenerator, final int size) {
    ArgumentChecker.notNull(nameGenerator, "nameGenerator");
    ArgumentChecker.notNull(positionGenerator, "positionGenerator");
    ArgumentChecker.isTrue(size > 0, "size");
    _nameGenerator = nameGenerator;
    _positionGenerator = positionGenerator;
    _size = size;
  }

  protected NameGenerator getNameGenerator() {
    return _nameGenerator;
  }

  protected PositionGenerator getPositionGenerator() {
    return _positionGenerator;
  }

  protected int getSize() {
    return _size;
  }

  @Override
  public PortfolioNode createPortfolioNode() {
    final SimplePortfolioNode node = new SimplePortfolioNode(getNameGenerator().createName());
    for (int i = 0; i < getSize(); i++) {
      Position position = getPositionGenerator().createPosition();
      // Note: the code below may be useful if the position generater sometimes fails to produce an entry and the portfolio must
      // contain the required amount. It is not useful if the position generator continually fails and you get an infinite loop.
      //while (position == null) {
      //  position = getPositionGenerator().createPosition();
      //}
      if (position != null) {
        node.addPosition(position);
      }
    }
    return node;
  }

}
