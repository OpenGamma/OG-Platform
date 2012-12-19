/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Fx_option',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, ui = og.common.util.ui;
            constructor.load = function () {
                constructor.title = 'FX Option';
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
                        extras: {},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'putCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'callCurrency'}})
                        ]
                    }),                    
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_option_date_tash',
                        extras: {},
                        children: [
                            new ui.Dropdown({
                                form: form, resource: 'blotter.exercisetypes', index: 'exerciseType',
                                value: '', rest_options: {}, placeholder: 'Select Exercise Type'
                            }),
                            new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'ITTODO'}})
                        ]
                        /*generator: function (handler, template, template_data) {
                            og.api.rest.aggregators.get().pipe(function (result) {
                                template_data.type = result.data;
                                handler(template(template_data));
                            });
                        }*/
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