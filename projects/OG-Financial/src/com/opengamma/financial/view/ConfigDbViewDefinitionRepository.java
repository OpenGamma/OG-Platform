/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory view definition repository which sources its {@link ViewDefinition}s from a {@link ConfigMaster} and
 * is kept up-to-date from this master.
 */
public class ConfigDbViewDefinitionRepository implements ViewDefinitionRepository, ChangeProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigDbViewDefinitionRepository.class);
  
  private static final String VIEW_DEFINITION_SCHEME = "ConfigDbViewDef";
  
  private final InMemoryViewDefinitionRepository _underlyingRepository;
  private final ConfigMaster _configMaster;
  private final ChangeManager _changeManager = new BasicChangeManager();
  private final ChangeListener _changeListener;
  
  public ConfigDbViewDefinitionRepository(ConfigMaster configMaster) {
    _configMaster = configMaster;
    _underlyingRepository = new InMemoryViewDefinitionRepository();
    
    ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    ConfigSearchResult<ViewDefinition> searchResult = _configMaster.search(request);
    for (ConfigDocument<ViewDefinition> configDocument : searchResult.getDocuments()) {
      _underlyingRepository.addViewDefinition(new AddViewDefinitionRequest(configDocument.getValue()));
    }
    _changeListener = new ChangeListener() {

      @Override
      public void entityChanged(ChangeEvent event) {
        ConfigDbViewDefinitionRepository.this.entityChanged(event);
      }
      
    };
    _configMaster.changeManager().addChangeListener(_changeListener);
  }
  
  @Override
  public Set<String> getDefinitionNames() {
    return _underlyingRepository.getDefinitionNames();
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
    UniqueIdentifier beforeId = null;
    UniqueIdentifier afterId = null;
    switch (event.getType()) {
      case ADDED:
      case UPDATED:
      case CORRECTED:
        if (event.beforeId() != null) {
          ConfigDocument<?> oldDocument = getConfigMaster().get(event.getBeforeId());
          if (!(oldDocument.getValue() instanceof ViewDefinition)) {
            return;
          }
          ViewDefinition oldDefinition = (ViewDefinition) oldDocument.getValue();
          beforeId = getUniqueId(oldDefinition.getName());
        }
        ConfigDocument<?> newDocument = getConfigMaster().get(event.getAfterId());
        if (!(newDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        // NOTE jonathan 2011-08-02 -- assumes name of view definition won't change
        ViewDefinition newDefinition = (ViewDefinition) newDocument.getValue();
        getUnderlyingRepository().addViewDefinition(new AddViewDefinitionRequest(newDefinition));
        afterId = getUniqueId(newDefinition.getName());
        break;
      case REMOVED:
        ConfigDocument<?> removedDocument = getConfigMaster().get(event.getBeforeId());
        if (!(removedDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        ViewDefinition removedDefinition = (ViewDefinition) removedDocument.getValue();
        try {
          getUnderlyingRepository().removeViewDefinition(removedDefinition.getName());
        } catch (DataNotFoundException e) {
          s_logger.warn("Attempted to remove view definition '" + removedDefinition.getName() + "' in response to change notification, but it was not found", e);
        }
        beforeId = getUniqueId(removedDefinition.getName());
        break;
    }
    changeManager().entityChanged(event.getType(), beforeId, afterId, event.getVersionInstant());
  }

  private UniqueIdentifier getUniqueId(String definitionName) {
    // REVIEW jonathan 2011-08-02 -- as an intermediate step to get change notifications to the engine, but without
    // having to change view definition names to unique identifiers everywhere, we cram the definition name into a
    // unique identifier for listeners to extract. Long-term, the actual master change event should be passed to the
    // engine.
    return UniqueIdentifier.of(VIEW_DEFINITION_SCHEME, definitionName);
  }
  
  private ConfigMaster getConfigMaster() {
    return _configMaster;
  }
  
  private InMemoryViewDefinitionRepository getUnderlyingRepository() {
    return _underlyingRepository;
  }

}
