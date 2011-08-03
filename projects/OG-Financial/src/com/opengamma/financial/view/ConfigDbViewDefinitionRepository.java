/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Set;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.listener.MasterChanged;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory view definition repository which sources its {@link ViewDefinition}s from a {@link ConfigMaster} and
 * is kept up-to-date from this master.
 */
public class NotifyingConfigDbViewDefinitionRepository implements ViewDefinitionRepository, MasterChangeListener, NotifyingMaster {

  private static final String VIEW_DEFINITION_SCHEME = "ViewDefinition";
  
  private final InMemoryViewDefinitionRepository _underlyingRepository;
  private final ConfigMaster _configMaster;
  private final MasterChangeManager _changeManager = new BasicMasterChangeManager();
  
  public NotifyingConfigDbViewDefinitionRepository(ConfigMaster configMaster) {
    _configMaster = configMaster;
    _underlyingRepository = new InMemoryViewDefinitionRepository();
    
    ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    ConfigSearchResult<ViewDefinition> searchResult = _configMaster.search(request);
    for (ConfigDocument<ViewDefinition> configDocument : searchResult.getDocuments()) {
      _underlyingRepository.addViewDefinition(new AddViewDefinitionRequest(configDocument.getValue()));
    }
    _configMaster.changeManager().addChangeListener(this);
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
  public void masterChanged(MasterChanged event) {
    ArgumentChecker.notNull(event, "event");
    UniqueIdentifier beforeId = null;
    UniqueIdentifier afterId = null;
    switch (event.getType()) {
      case ADDED:
      case CORRECTED:
      case UPDATED:
        ConfigDocument<?> newDocument = getConfigMaster().get(event.getAfterId());
        if (!(newDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        // REVIEW jonathan 2011-08-02 -- assumes name of view definition won't change
        ViewDefinition newDefinition = (ViewDefinition) newDocument.getValue();
        getUnderlyingRepository().addViewDefinition(new AddViewDefinitionRequest(newDefinition));
        afterId = UniqueIdentifier.of(VIEW_DEFINITION_SCHEME, newDefinition.getName());
        break;
      case REMOVED:
        ConfigDocument<?> removedDocument = getConfigMaster().get(event.getBeforeId());
        if (!(removedDocument.getValue() instanceof ViewDefinition)) {
          return;
        }
        ViewDefinition removedDefinition = (ViewDefinition) removedDocument.getValue();
        getUnderlyingRepository().removeViewDefinition(removedDefinition.getName());
        beforeId = UniqueIdentifier.of(VIEW_DEFINITION_SCHEME, removedDefinition.getName());
        break;
    }
    
    // REVIEW jonathan 2011-08-02 -- as an intermediate step to get change notifications to the engine, but without
    // having to change view definition names to unique identifiers everywhere, we cram the definition name into a
    // unique identifier for listeners to extract. Long-term, the actual master change event should be passed to the
    // engine.
    changeManager().masterChanged(event.getType(), beforeId, afterId, event.getVersionInstant());
  }
  
  //-------------------------------------------------------------------------  
  private ConfigMaster getConfigMaster() {
    return _configMaster;
  }
  
  private InMemoryViewDefinitionRepository getUnderlyingRepository() {
    return _underlyingRepository;
  }

  @Override
  public MasterChangeManager changeManager() {
    return _changeManager;
  }

}
