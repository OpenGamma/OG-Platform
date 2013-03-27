/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.legacyfixedrecoverycds',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Legacy Fixed Recovery CDS';
            config.type = 'LegacyFixedRecoveryCDS';
            config.legacy = true;
            return new og.blotter.forms.cds(config);
        };    
    }
});