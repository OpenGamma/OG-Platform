/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.opengamma.util.ArgumentChecker;

/**
 * Manages the relationship between trades and positions for a position load process.
 * The resolver is initially populated with a set of trade ids. Position trade
 * relationships can then be added and when complete the {@link #resolve} method called.
 * This freezes the state of the resolver (no more relationships can be added), and
 * the following are calculated:
 * <ul>
 * <li>positions with their associated trades</li>
 * <li>trades from the trade population with no associated position</li>
 * <li>trades in positions which were not in the original trade set</li>
 * <li>trades that appear in multiple positions</li>
 * </ul>
 * These can be examined by the client and errors raised as appropriate.
 */
public class TradePositionResolver {

  /**
   * The set of trade ids which are expected to be handled. Attempts to create
   * a position where the trade is unknown will be flagged as errors.
   */
  private final Set<String> _tradeIds;

  /**
   * The builder used to construct the {@link #_positions} multimap once the
   * {@link #resolve()} method has been called. The builder is used in
   * preference to a mutable map due to the convenience of using the
   * {@link ImmutableMultimap#inverse()} method when constructing the
   * {@link #_invertedPositions}.
   *
   */
  private final ImmutableMultimap.Builder<String, String> _positionBuilder = ImmutableMultimap.builder();

  /**
   * Multimap of positions -> trades. It will be quite usual to have a position
   * containing multiple trades. This map is not populated until the
   * {@link #resolve()} method has been called.
   */
  private ImmutableMultimap<String, String> _positions;

  /**
   * The inverse of the position map, so containing trades -> positions. Generally we
   * are not expecting a trade to be in more than one position but it may be possible
   * in the future. This map is not populated until the {@link #resolve()} method has
   * been called.
   */
  private ImmutableMultimap<String, String> _invertedPositions;

  /**
   * The set of trades which were in the initial set of trade ids but were not
   * referenced in any of the positions. This Iterable is not populated until the
   * {@link #resolve()} method has been called.
   */
  private Iterable<String> _orphanTrades;

  /**
   * Multimap of trades -> positions indicating trades which appear in more than
   * one position (along with the positions they appear in). It is for the client to
   * determine whether this is acceptable or not, and how to resolve. This map is not
   * populated until the {@link #resolve()} method has been called.
   */
  private ImmutableMultimap<String, String> _duplicateTrades;

  /**
   * The set of trades which were referenced by positions but were not in the initial
   * set of trade ids. It is for the client to determine if this is an error and how
   * to resolve. This Iterable is not populated until the {@link #resolve()} method has
   * been called.
   */
  private Iterable<String> _unknownTrades;

  private ImmutableMultimap<String, String>  _portfolioPositions;

  private ImmutableMultimap<String, String>  _portfolioTrades;

  private ImmutableMultimap<String, String>  _portfolioPortfolios;

  /**
   * Maintains the state of the resolver. Positions can only be added whilst we are
   * in the unresolved state, accessor methods can only be called when we are in the
   * resolved state.
   */
  private boolean _isResolved;

  /**
   * Constructor, taken in the set of known trade ids. Trade ids which are referenced
   * by position but are not in the initial set can be determined from the
   * {@link #getUnknownTrades()} method.
   *
   * @param tradeIds the known set of trade ids
   */
  public TradePositionResolver(Set<String> tradeIds) {
    ArgumentChecker.notNull(tradeIds, "_tradeIds");
    _tradeIds = tradeIds;
  }

  /**
   * Returns a Multimap of positions -> trades built from the positions added via the
   * {@link #addToPosition(String, String)} method. It will be quite usual to have a position
   * containing multiple trades. Note that this map is not populated until the
   * {@link #resolve()} method has been called. Any attempt to access before that point will
   * result in an IllegalStateException being thrown.
   *
   * @return Multimap of position -> trades
   * @throws IllegalStateException if this method is called before the {@link #resolve()} method
   */
  public Multimap<String, String> getPositions() {
    checkResolved();
    return _positions;
  }

  /**
   * Returns the set of trades which were in the initial set of trade ids but which have not
   * been referenced by any of the positions. Note that this Iterable is not populated until
   * the {@link #resolve()} method has been called. Any attempt to access before that point will
   * result in an IllegalStateException being thrown.
   *
   * @return the set of orphan trades
   * @throws IllegalStateException if this method is called before the {@link #resolve()} method
   */
  public Iterable<String> getOrphans() {
    checkResolved();
    return _orphanTrades;
  }

  /**
   * Returns a Multimap of trades -> positions indicating trades which appear in more than
   * one position (along with the positions they appear in). It is for the client to
   * determine whether this is acceptable or not, and how to resolve. Note that this map
   * is not populated until the {@link #resolve()} method has been called.
   *
   * @return Multimap of trade -> positions
   * @throws IllegalStateException if this method is called before the {@link #resolve()} method
   */
  public Multimap<String, String> getDuplicateTrades() {
    checkResolved();
    return _duplicateTrades;
  }

  /**
   * Returns the set of trades which were referenced by positions but were not in the initial
   * set of trade ids. It is for the client to determine if this is an error and how to resolve.
   * Note that this Iterable is not populated until the {@link #resolve()} method has been called.
   *
   * @return the set of unknown trades
   * @throws IllegalStateException if this method is called before the {@link #resolve()} method
   */
  public Iterable<String> getUnknownTrades() {
    checkResolved();
    return _unknownTrades;
  }

  /**
   * Record an association between the trade and position. Note that duplicate
   * position / trade combinations are accepted and therefore the client should
   * ensure uniqueness before calling, if that is what is required. Note that this
   * method cannot be called after the {@link #resolve()} method has been called.
   *
   * @param positionId the position to be added to
   * @param tradeId the trade to be added to the position
   * @throws IllegalStateException if this method is called after the {@link #resolve()} method
   */
  public void addToPosition(String positionId, String tradeId) {
    if (_isResolved) {
      throw new IllegalStateException("Cannot add position data as resolve() method has been called.");
    }
    _positionBuilder.put(positionId, tradeId);
  }

  public void addPositionToPortfolio(String portfolioId, String positionId) {

  }

  public void addTradeToPortfolio(String portfolioId, String tradeId) {

  }

  /**
   * Mark the resolved as resolved and perform the calculations required. After calling
   * this method, calls to the accessor methods are enabled and calls to
   * {@link #addToPosition(String, String)} will be disallowed.
   */
  public void resolve() {

    if (!_isResolved) {
      _isResolved = true;
      _positions = _positionBuilder.build();
      _invertedPositions = _positions.inverse();
      _orphanTrades = determineOrphanTrades();
      _duplicateTrades = ImmutableMultimap.copyOf(determineDuplicatedTrades());
      _unknownTrades = determineUnknownTrades();
    }
  }

  private void checkResolved() {
    if (!_isResolved) {
      throw new IllegalStateException("Cannot access resolved data until resolve() method is called.");
    }
  }

  private Iterable<String> determineOrphanTrades() {
    return ImmutableSet.copyOf(Iterables.filter(_tradeIds,
        new Predicate<String>() {
          @Override
          public boolean apply(String tradeId) {
            return !_invertedPositions.containsKey(tradeId);
          }
        }
    ));
  }

  private Multimap<String, String> determineDuplicatedTrades() {
    return Multimaps.filterKeys(_invertedPositions,
        new Predicate<String>() {
          @Override
          public boolean apply(String s) {
            return _invertedPositions.get(s).size() > 1;
          }
        }
    );
  }

  private Iterable<String> determineUnknownTrades() {
    return Iterables.filter(_invertedPositions.keySet(),
        new Predicate<String>() {
          @Override
          public boolean apply(String s) {
            return !_tradeIds.contains(s);
          }
        }
    );
  }
}
