/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Edit_fx_option',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'FX Option Termination';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.edit_fx_option_tash',
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