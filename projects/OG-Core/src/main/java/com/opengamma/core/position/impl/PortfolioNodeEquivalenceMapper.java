/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.UniqueId;

/**
 * Tests nodes in one portfolio for equivalence with nodes from another. All portfolio nodes and positions within the structure must have valid unique identifiers.
 * <p>
 * Portfolio nodes currently contain a reference to their parent nodes, so changing one within a portfolio is not possible without changing the entire portfolio node graph. This may be used to map the
 * logically unchanged nodes from one portfolio structure to nodes in another so that incremental operations may be performed.
 * <p>
 * If the {@link PortfolioNode} interfaces loses its {@link PortfolioNode#getParentNodeId} member, this should no longer be necessary.
 * <p>
 * Two nodes A and B are normally considered equivalent if their names match, their immediate child positions match, and there is an equivalence mapping of all of the child nodes of A to and from all
 * of the child nodes of B.
 */
public class PortfolioNodeEquivalenceMapper {

  /**
   * Tests if the labels of two nodes are equal. Subclasses might relax this test to allow cheap name adjustments on a portfolio if the function repository is known not to use the name of the node
   * when determining how to calculate aggregate values.
   * 
   * @param a the first node to test, not null
   * @param b the second node to test, not null
   * @return true if the names are equivalent, false otherwise
   */
  protected boolean isNameMatch(final PortfolioNode a, final PortfolioNode b) {
    return a.getName().equals(b.getName());
  }

  /**
   * Tests if the positions immediately under two nodes are equal. Subclasses might relax this test to allow reordering of positions within a node if the function repository is known not to use the
   * order of the positions when calculating aggregate values.
   * <p>
   * The test for equality is made solely on the unique identifier of the positions.
   * 
   * @param a the first node to test, not null
   * @param b the second node to test, not null
   * @return true if the positions match, false otherwise
   */
  protected boolean isPositionMatch(final PortfolioNode a, final PortfolioNode b) {
    final List<Position> as = a.getPositions();
    final List<Position> bs = b.getPositions();
    if (as.size() != bs.size()) {
      return false;
    }
    final Iterator<Position> itrA = as.iterator();
    final Iterator<Position> itrB = bs.iterator();
    while (itrA.hasNext()) {
      if (!itrA.next().getUniqueId().equals(itrB.next().getUniqueId())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests if the names and immediate child positions match.
   * 
   * @param a the first node to test, not null
   * @param b the second node to test, not null
   * @return true if the nodes match, false otherwise
   */
  protected boolean isMatch(final PortfolioNode a, final PortfolioNode b) {
    return isNameMatch(a, b) && isPositionMatch(a, b);
  }

  /**
   * Maps as many of the nodes from {@code as} to as many of the nodes from {@code bs} as possible.
   * 
   * @param as a collection of nodes, not null and not containing null
   * @param bs a collection of nodes, not null and not containing null
   * @param result the resulting mapping of equivalent nodes
   * @return true if all of the nodes from {@code as} were mapped to all of the nodes from {@code bs}
   */
  protected boolean getEquivalentNodes(final Collection<PortfolioNode> as, final Collection<PortfolioNode> bs, final Map<UniqueId, UniqueId> result) {
    if (as.isEmpty() && bs.isEmpty()) {
      return true;
    }
    final PortfolioNode[] bsa = bs.toArray(new PortfolioNode[bs.size()]);
    int bsaLength = bsa.length;
    boolean allMatched = true;
    nextA: for (PortfolioNode a : as) { //CSIGNORE
      for (int i = 0; i < bsaLength; i++) {
        final PortfolioNode b = bsa[i];
        if (isMatch(a, b)) {
          if (getEquivalentNodes(a.getChildNodes(), b.getChildNodes(), result)) {
            result.put(a.getUniqueId(), b.getUniqueId());
            bsa[i] = bsa[--bsaLength];
            continue nextA;
          }
        }
      }
      allMatched = false;
    }
    return allMatched && (bsaLength == 0);
  }

  /**
   * Maps the node {@code a} and any child nodes to the equivalent node {@code b} and any child nodes if possible. The keys of the result are from {@code a}, the values are from {@code b}. Any nodes
   * that could not be mapped are absent from the collection. If {@code a} and {@code b} are equivalent then the map will contain an entry for {@code (a, b)} plus all child nodes. If {@code a} and
   * {@code b} are not equivalent but contain child nodes that are then the map will contain those child nodes.
   * <p>
   * This will typically be used with the root nodes from two portfolios to be compared.
   * 
   * @param a the root node to map from, not null and not containing null
   * @param b the root node to map to, not null and not containing null
   * @return the mapping, not null and not containing null
   */
  public Map<UniqueId, UniqueId> getEquivalentNodes(final PortfolioNode a, final PortfolioNode b) {
    final Map<UniqueId, UniqueId> result = new HashMap<UniqueId, UniqueId>();
    if (getEquivalentNodes(a.getChildNodes(), b.getChildNodes(), result) && isMatch(a, b)) {
      result.put(a.getUniqueId(), b.getUniqueId());
    }
    return result;
  }

}
