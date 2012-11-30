/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Forward_rate_agreement',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'Bond';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.forward_rate_agreement_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.dom();
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});