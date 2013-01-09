/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Non_del_fx_option',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'Non-Deliverable FX Option';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.long_short_tash',
                        extras: {}
                    }), 
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_value_tash',
                        extras: {}
                    }),                    
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_date_tash',
                        extras: {nondev:true}
                    }),
                    new og.common.util.ui.Attributes({form: form})
                );
                form.dom();   
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});