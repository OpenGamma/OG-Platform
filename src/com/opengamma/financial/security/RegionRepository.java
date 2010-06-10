/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import javax.time.calendar.LocalDate;

/**
 * Interface for retrieving Region hierarchies.
 */
public interface RegionRepository {
  /**
   * Return the root node of the named region hierarchy
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param hierarchyName the name of the hierarchy
   * @return the root node of the hierarchy
   */
  Region getHierarchyRoot(final LocalDate asOf, final String hierarchyName);
  
  /**
   * Get the region information of the named node in the hierarchy with the supplied name.
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param hierarchyName the name of the hierarchy
   * @param nodeName the name of that Region
   * @return the matching region
   */
  Region getHierarchyNode(final LocalDate asOf, final String hierarchyName, final String nodeName);
  
  /**
   * Get all region nodes of a particular type
   * @param asOf the data returned is that considered correct as of the provided date
   * @param hierarchyName the name of the hierarchy
   * @param type the type of the region object
   * @return a set of all the region objects in a hierarchy of the specified type
   */
  Set<Region> getAllOfType(final LocalDate asOf, final String hierarchyName, final RegionType type);
  
  /**
   * @param asOf the data returned is that considered correct as of the provided date
   * @param hierarchyName the name of the hierarchy
   * @param fieldName the name of the field to find
   * @param value the value that the field must have to be returned
   * @return a set of Regions which have the appropriate field with the appropriate value
   */
  Set<Region> getHierarchyNodes(final LocalDate asOf, final String hierarchyName, final String fieldName, final Object value);
}
