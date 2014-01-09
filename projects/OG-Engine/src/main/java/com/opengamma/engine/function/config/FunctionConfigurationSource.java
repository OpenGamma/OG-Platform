/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.util.PublicSPI;

/**
 * Provides a function configuration for bootstrapping a function repository. Possible implementations may (but are not limited to):
 * <ul>
 * <li>Retrieve function information from a configuration database;
 * <li>Generate function information dynamically (e.g. by scanning for annotations);
 * <li>Download the configuration from another node;
 * <li>Collate the information from other {@code FunctionConfigurationSource} instances; or
 * <li>Construct the repository from static data.
 * </ul>
 */
@PublicSPI
public interface FunctionConfigurationSource extends ChangeProvider {

  /**
   * The object identifier that this source will report changes to.
   */
  ObjectId OBJECT_ID = ObjectId.of("OpenGamma", "FunctionConfiguration");

  /**
   * Returns a function configuration bundle.
   * <p>
   * A system will typically run with a consistent configuration. To allow for atomic update behavior, and changes to configuration during calculation cycles, a "version" instant is provided. When
   * working on jobs within a cycle, all remote nodes will request configuration with the same version stamp that the view process marked those job with. This means that all nodes will be working with
   * the same function repository regardless of configuration changes that may have taken place during that cycle. The next cycle will use jobs that are tagged with an updated version so the results
   * appear coherent.
   * <p>
   * Note that version 2.2 and earlier of OpenGamma did not have the {@code version} parameter on this method. If migrating implementation code from an earlier version, the value of the parameter
   * should be ignored as the previous contract should have been to return the same configuration with each call. If migrating calling code from an earlier version, using {@link Instant#now} is
   * reasonable, but the exact equivalent should be to use the time when the OpenGamma instance was started.
   * 
   * @param version the version timestamp, not null
   * @return the configuration, not null
   */
  FunctionConfigurationBundle getFunctionConfiguration(Instant version);

}
