/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.opengamma.component.ComponentRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * A listener for the servlet context creation.
 * <p>
 * This connects the creation of the {@code ServletContext} with the {@code ComponentRepository}.
 */
public class ComponentRepositoryServletContextListener implements ServletContextListener {

  /**
   * The component repository.
   */
  private final ComponentRepository _repo;

  /**
   * Creates an instance.
   * 
   * @param repo  the repository, not null
   */
  public ComponentRepositoryServletContextListener(ComponentRepository repo) {
    ArgumentChecker.notNull(repo, "repo");
    _repo = repo;
  }

  //-------------------------------------------------------------------------
  @Override
  public void contextInitialized(ServletContextEvent event) {
    _repo.setServletContext(event.getServletContext());
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    _repo.stop();
  }

}
