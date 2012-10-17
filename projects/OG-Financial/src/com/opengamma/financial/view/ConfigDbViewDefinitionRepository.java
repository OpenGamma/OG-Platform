/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigMasterIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory view definition repository which sources view definitions from a config master.
 * <p>
 * This loads {@link ViewDefinition}s from a {@link ConfigMaster} and is kept up-to-date from this master.
 */
public class ConfigDbViewDefinitionRepository implements ViewDefinitionRepository, ChangeProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConfigDbViewDefinitionRepository.class);

  /**
   * The underlying repository.
   */
  private final InMemoryViewDefinitionRepository _underlyingRepository;
  /**
   * The config master storing the view definitions.
   */
  private final ConfigMaster _configMaster;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager = new BasicChangeManager();
  /**
   * The change listener.
   */
  private final ChangeListener _changeListener;

  /**
   * Creates an instance.
   * 
   * @param configMaster  the config master, not null
   */
  public ConfigDbViewDefinitionRepository(ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
    _underlyingRepository = new InMemoryViewDefinitionRepository();
    
    ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    for (ConfigDocument<ViewDefinition> doc : ConfigMasterIterator.iterable(configMaster, request)) {
      _underlyingRepository.addViewDefinition(new AddViewDefinitionRequest(doc.getValue()));
    }
    _changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        ConfigDbViewDefinitionRepository.this.entityChanged(event);
      }
    };
    _configMaster.changeManager().addChangeListener(_changeListener);
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<ObjectId> getDefinitionIds() {
    return _underlyingRepository.getDefinitionIds();
  }

  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    return _underlyingRepository.getDefinition(definitionId);
  }

  @Override
  public Map<UniqueId, String> getDefinitionEntries() {   
    return _underlyingRepository.getDefinitionEntries();    
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    return _underlyingRepository.getDefinition(definitionName);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  private void entityChanged(ChangeEvent event) {
    ArgumentChecker.notNull(event, "event");
    UniqueId beforeId = null;
    UniqueId afterId = null;
    switch (event.getType()) {
      case ADDED:
      case UPDATED:
      case CORRECTED:
        if (event.getBeforeId() != null) {
          ConfigDocument<?> oldDocument = getConfigMaster().get(event.getBeforeId());
          if (!(oldDocument.getValue() instanceof ViewDefinition)) {
            return;
          }
          ViewDefinition oldDefinition = (ViewDefinition) oldDocument.getValue();
          beforeId = oldDefinition.getUniqueId();
        }
        ConfigDocument<?> newDocument = getConfigMaster().get(event.getAfterId());
        if (!(newDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        ViewDefinition newDefinition = (ViewDefinition) newDocument.getValue();
        afterId = getUnderlyingRepository().addViewDefinition(new AddViewDefinitionRequest(newDefinition));
        break;
      case REMOVED:
        ConfigDocument<?> removedDocument = getConfigMaster().get(event.getBeforeId());
        if (!(removedDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        ViewDefinition removedDefinition = (ViewDefinition) removedDocument.getValue();
        try {
          getUnderlyingRepository().removeViewDefinition(removedDefinition.getUniqueId());
        } catch (DataNotFoundException ex) {
          s_logger.warn("Attempted to remove view definition '" + removedDefinition.getName() + "' in response to change notification, but it was not found", ex);
        }
        beforeId = removedDefinition.getUniqueId();
        break;
    }
    changeManager().entityChanged(event.getType(), beforeId, afterId, event.getVersionInstant());
  }

  private ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  private InMemoryViewDefinitionRepository getUnderlyingRepository() {
    return _underlyingRepository;
  }

}
