/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Bond',
    dependencies: [],
    obj: function () {   
        return function () {
            var contructor = this;
            contructor.load = function () {
                var config = {}, dialog; 
                config.title = 'Bond';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.bond_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    new form.Block({
                        module: 'og.blotter.forms.block.security_tash',
                        extras: {}
                    }),
                     new form.Block({
                        module: 'og.blotter.forms.block.security_ids_tash',
                        extras: {}
                    })
                );
                form.dom();
                $('.OG-blotter-form-title').html(config.title);
            }; 
            contructor.load();
            contructor.kill = function () {
            };
        };
    }
});