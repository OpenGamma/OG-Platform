/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata;

import java.util.Map;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.PublicSPI;

/**
 * A provider of reference data for Bloomberg.
 * <p>
 * This provides access to a data source for reference data information.
 * Reference data is a low-level API for a data source that requires knowledge of the data source.
 * This interface is Bloomberg specific.
 * <p>
 * The identifier specified below is typically the Bloomberg BUID.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface ReferenceDataProvider {

  /**
   * Gets the reference data for a single field of a single identifier.
   * <p>
   * This retrieves the field reference data for the identifier.
   * 
   * @param identifier  the identifier, not null
   * @param dataField  the field to retrieve, not null
   * @return the result, null if not found
   * @throws RuntimeException if an unexpected error occurs
   */
  String getReferenceDataValue(String identifier, String dataField);

  /**
   * Gets the reference data for a set of fields of a single identifier.
   * <p>
   * This retrieves the field reference data for the identifier.
   * The map will have missing elements, not nulls, when a field is not found.
   * 
   * @param identifier  the identifier, not null
   * @param dataFields  the fields to retrieve, not null
   * @return the field-value map, not null
   * @throws RuntimeException if an unexpected error occurs
   */
  Map<String, String> getReferenceDataValues(String identifier, Iterable<String> dataFields);

  /**
   * Gets the reference data for a set of fields of a single identifier.
   * <p>
   * This retrieves the field reference data for the identifier.
   * The map will have missing elements, not nulls, when an identifier is not found.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataField  the field to retrieve, not null
   * @return the identifier-value map, not null
   * @throws RuntimeException if an unexpected error occurs
   */
  Map<String, String> getReferenceDataValues(Iterable<String> identifiers, String dataField);

  /**
   * Gets the reference data for a set of fields of a set of identifiers.
   * <p>
   * This retrieves the field reference data for the identifiers.
   * The map will have missing elements, not nulls, when an identifier or field is not found.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataFields  the fields to retrieve, not null
   * @return the reference data, from security to field-value map, not null
   * @throws RuntimeException if an unexpected error occurs
   */
  Map<String, FudgeMsg> getReferenceData(Iterable<String> identifiers, Iterable<String> dataFields);

  /**
   * Gets the reference data for a set of fields of a set of identifiers ignoring caching.
   * <p>
   * This retrieves the field reference data for the identifiers.
   * This will ignore any caching and directly call the underlying data source.
   * The map will have missing elements, not nulls, when an identifier or field is not found.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataFields  the fields to retrieve, not null
   * @return the reference data, from security to field-value map, not null
   * @throws RuntimeException if an unexpected error occurs
   */
  Map<String, FudgeMsg> getReferenceDataIgnoreCache(Iterable<String> identifiers, Iterable<String> dataFields);

  /**
   * Gets one or more pieces of reference data from the underlying data source.
   * <p>
   * The result must contain an entry for every identifier in the input request.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the reference data result, not null
   * @throws RuntimeException if an unexpected error occurs
   */
  ReferenceDataProviderGetResult getReferenceData(ReferenceDataProviderGetRequest request);

}
