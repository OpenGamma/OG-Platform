/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.creditdefaultswapindexsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'CDS Index';
            config.type = 'CreditDefaultSwapIndexSecurity';
            return new og.blotter.forms.cds(config);
        };
    }
});