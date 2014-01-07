/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxoptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'FX Option';
            config.type = 'FXOptionSecurity';
            config.fxoption = true;
            return new og.blotter.forms.fxoptionbasesecurity(config);
        };
    }
});