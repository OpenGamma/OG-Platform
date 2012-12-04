/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Vanilla_swap',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'Vanilla Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.vanilla_swap_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash',
                        extras: {}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {}
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_fixed_tash',
                        extras: {}
                    }) ,
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_floating_tash',
                        extras: {}
                    }),
                    new og.common.util.ui.Attributes({form: form})
                );
                form.dom();
            }; 
            constructor.kill = function () {
            };
            constructor.load();
        };
    }
});
