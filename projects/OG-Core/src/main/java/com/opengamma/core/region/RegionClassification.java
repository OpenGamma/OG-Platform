/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import com.opengamma.util.PublicAPI;

/**
 * The classification of a region.
 * <p>
 * Regions have been classified into fixed a fixed set of types.
 * The naming and classification of regions is the source of many political and diplomatic disagreements.
 * The comments used here are indicative of the data stored in the underlying data source.
 */
@PublicAPI
public enum RegionClassification {

  /**
   * The root of the hierarchy, such as the world.
   */
  ROOT,
  /**
   * A super-national grouping, such as Europe, Asia and EuroZone.
   */
  SUPER_NATIONAL,
  /**
   * A state that is de jure independent.
   * Frequently cited examples are the UK, France and USA.
   */
  INDEPENDENT_STATE,
  /**
   * A state that is de facto independent, but not de jure.
   * Frequently cited example are Taiwan and Northern Cyprus.
   */
  PROTO_INDEPENDENT_STATE,
  /**
   * A dependency of an independent state.
   * Frequently cited examples are Jersey (UK) and Bermuda (USA).
   */
  DEPENDENCY,
  /**
   * An area of an independent state that resembles a dependency, but is not one.
   * Frequently cited examples are Greenland (Denmark) or Hong Kong (China).
   */
  PROTO_DEPENDENCY,
  /**
   * A territory with disputed sovereignty.
   * Frequently cited examples are Western Sahara and Kosovo.
   */
  DISPUTED_TERRITORY,
  /**
   * A territory that is part of Antartica and is suspended under the Antarctic Treaty.
   * Frequently cited examples are Ross Dependency (New Zealand) and Queen Maud Land (Norway).
   */
  ANTARCTIC_TERRITORY,
  /**
   * A sub-grouping within a state or territory, such as Norfolk in England or Texas in the USA.
   * The local name of the sub-division varies greatly, examples being County, State and Parish.
   * The sub-division terminology avoids referring to any of these local names.
   */
  SUB_DIVISION,
  /**
   * A particular municipal area within a sub-division, such as London.
   */
  MUNICIPALITY,

}
