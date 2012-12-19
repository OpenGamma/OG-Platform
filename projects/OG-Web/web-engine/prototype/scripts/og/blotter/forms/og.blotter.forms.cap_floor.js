/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Cap_floor',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, ui = og.common.util.ui;
            constructor.load = function () {
                constructor.title = 'Cap/Floor';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.cap_floor_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cap_floor_tash',
                        extras: {},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash'}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'frequency',
                                value: '', rest_options: {}, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: 'dayCount',
                                value: '', rest_options: {}, placeholder: 'Select Day Count'
                            })
                        ]
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