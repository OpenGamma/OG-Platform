/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.standardfixedrecoverycdssecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Standard Fixed Recovery CDS';
            config.type = 'StandardFixedRecoveryCDSSecurity';
            return new og.blotter.forms.cds(config);
        };
    }
});