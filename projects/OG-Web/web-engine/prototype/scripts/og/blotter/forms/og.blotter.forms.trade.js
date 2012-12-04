/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Trade',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this;
            constructor.load = function () {
                constructor.title = 'Trade';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.trade_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new form.Block({
                        module: 'og.blotter.forms.blocks.security_tash',
                        extras: {}
                    }),
                     new form.Block({
                        module: 'og.blotter.forms.blocks.security_ids_tash',
                        extras: {}
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