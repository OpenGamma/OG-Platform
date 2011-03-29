/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Produce a {@link ProcedureProvider} from a set of {@link PublishedProcedure} objects.
 */
public class ProcedureProviderBean extends AbstractProcedureProvider implements InitializingBean {

  private Collection<PublishedProcedure> _procedures;

  public void setProcedures(final Collection<PublishedProcedure> procedures) {
    ArgumentChecker.notNull(procedures, "procedures");
    _procedures = new ArrayList<PublishedProcedure>(procedures);
  }

  private Collection<PublishedProcedure> getProceduresInternal() {
    return _procedures;
  }

  @SuppressWarnings("unchecked")
  public Collection<PublishedProcedure> getProcedures() {
    return Collections.unmodifiableCollection(getProceduresInternal());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getProceduresInternal(), "procedures");
  }

  // AbstractProcedureProvider

  @Override
  protected void loadDefinitions(final Collection<MetaProcedure> definitions) {
    for (PublishedProcedure procedure : getProceduresInternal()) {
      definitions.add(procedure.getMetaProcedure());
    }
  }

}
