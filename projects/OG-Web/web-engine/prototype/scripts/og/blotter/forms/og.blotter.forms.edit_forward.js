/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Edit_forward',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'Forward Liquidation';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.edit_forward_tash',
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