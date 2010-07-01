/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for retrieving Region hierarchies.
 */
public interface RegionRepository {
  /**
   * Return the root node of the named region hierarchy
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param hierarchyName the name of the hierarchy
   * @return the root node of the hierarchy or null if no match
   */
  Region getHierarchyRoot(final LocalDate asOf, final String hierarchyName);
  
  /**
   * Get the region information of the named node in the hierarchy with the supplied name.
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param hierarchyName the name of the hierarchy
   * @param nodeName the display name associated with that Region
   * @return the matching region or null if none found
   */
  Region getHierarchyNode(final LocalDate asOf, final String hierarchyName, final String nodeName);
  
  /**
   * Get the region information of the named node in the hierarchy with the supplied name.
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param hierarchyName the name of the hierarchy
   * @param nodeId an Identifier associated with the desired region
   * @return the matching region or null if none is found
   */
  Region getHierarchyNode(final LocalDate asOf, final String hierarchyName, final Identifier nodeId);

  /**
   * Get the region information using the unique id assigned to a region/hierarchy combination.
   * @param asOf the data returned is that considered correct as of the provided date 
   * @param nodeId an Identifier associated with the desired region
   * @return the matching region or null if none is found
   */
  Region getHierarchyNode(final LocalDate asOf, final UniqueIdentifier uniqueId);
  
  
  /**
   * Get all region nodes of a particular type
   * @param asOf the data returned is that considered correct as of the provided date
   * @param hierarchyName the name of the hierarchy
   * @param type the type of the region object
   * @return a set of all the region objects in a hierarchy of the specified type
   */
  SortedSet<Region> getAllOfType(final LocalDate asOf, final String hierarchyName, final RegionType type);
  
  /**
   * @param asOf the data returned is that considered correct as of the provided date
   * @param hierarchyName the name of the hierarchy
   * @param fieldId the name of the field to find
   * @param value the value that the field must have to be returned
   * @return a set of Regions which have the appropriate field with the appropriate value
   */
  SortedSet<Region> getHierarchyNodes(final LocalDate asOf, final String hierarchyName, final String fieldName, final Object value);

  /**
   * @param asOf the data returned is that considered correct as of the provided date
   * @param hierarchyName the name of the hierarchy
   * @param fieldNameValuePairs 0..n name->value pairs that should match
   * @return a set of Regions which have the appropriate fields with the appropriate values
   */
  SortedSet<Region> getHierarchyNodes(LocalDate asOf, String hierarchyName, Pair<String, Object>... fieldNameValuePairs);
}
