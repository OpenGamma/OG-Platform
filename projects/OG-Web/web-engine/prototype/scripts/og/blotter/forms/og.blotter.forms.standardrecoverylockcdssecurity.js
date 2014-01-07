/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.standardrecoverylockcdssecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Standard Recovery Lock CDS';
            config.type = 'StandardRecoveryLockCDSSecurity';
            return new og.blotter.forms.cds(config);
        };
    }
});