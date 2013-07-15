/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.standardvanillacdssecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Standard Vanilla CDS';
            config.type = 'StandardVanillaCDSSecurity';
            config.standard = true;
            config.stdvanilla = true;
            return new og.blotter.forms.cds(config);
        };    
    }
});