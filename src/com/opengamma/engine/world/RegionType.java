/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.world;

/**
 * The type of region
 */
public enum RegionType {
  /**
   * Super national - e.g. Europe/Asia/EuroZone etc.
   */
  SUPER_NATIONAL,
  /**
   * A state that is de jure independent.  Examples would be both the UK and England
   */
  INDEPENDENT_STATE,
  /**
   * A state that is de facto independent, but not de jure.  E.g. Taiwan
   */
  PROTO_INDEPENDENT_STATE,
  /**
   * a territory of an independent state - e.g. Bermuda
   */
  DEPENDENCY,
  /**
   * an orea of an independent state that resembles a dependency, but is not one, e.g. Hong Kong.
   */
  PROTO_DEPENDENCY,
  /**
   * a territory with undetermined sovereignty - e.g. Western Sahara
   */
  DISPUTED_TERRITORY,
  /**
   * a territory that is part of Antartica and is suspended under the Antarctic Treaty - i.e. Ross Dependency
   */
  ANTARCTIC_TERRITORY,
  /**
   * A sub-grouping within a state or territory - equivalent to a state/county/parish etc.  Examples would be Norfolk in England or Maine in the US. 
   */
  SUB_DIVISION,
  /**
   * A particular municipal area within a sub division.  An example would be London.
   */
  MUNICIPALITY,
}
