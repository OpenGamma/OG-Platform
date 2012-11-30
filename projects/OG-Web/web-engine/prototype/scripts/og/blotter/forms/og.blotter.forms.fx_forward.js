/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Fx_forward',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'FX Forward';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_derivative_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_derivative_value_tash',
                        extras: {forward:true}
                    }),                    
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_derivative_date_tash',
                        extras: {}
                    })
                );
                form.dom();
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});