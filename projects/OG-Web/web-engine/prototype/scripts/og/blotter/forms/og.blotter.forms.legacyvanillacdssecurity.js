/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.legacyvanillacdssecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Legacy Vanilla CDS';
            config.type = 'LegacyVanillaCDSSecurity';
            return new og.blotter.forms.cds(config);
        };
    }
});