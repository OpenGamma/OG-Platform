/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Bond_future',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, bond = {}, ids = {}, security = {}, util = og.blotter.util, 
            dropdown = '.og-blotter-security-select';
            ids.selector = '.og-blocks-security_ids';
            constructor.load = function () {
                constructor.title = 'Bond Future';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.bond_future_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras:{}
                });
                form.children.push(
                    security.block = new form.Block({
                        module: 'og.blotter.forms.blocks.security_tash',
                        extras: {}
                    }),
                    bond.block = new form.Block({
                        module: 'og.blotter.forms.blocks.bond_future_tash',
                        extras: {}
                    }),
                    ids.block = new form.Block({
                        module: 'og.blotter.forms.blocks.security_ids_tash',
                        extras: {}
                    }),
                    new og.common.util.ui.Attributes({form: form})  
                );
                form.dom();
                form.on('form:load', function () {
                    var $select = $(dropdown);
                    util.FAKE_DROPDOWN.forEach(function (datum) {
                        $select.append(util.option({value: datum.value, name:datum.name}));
                    });
                });
                security.block.on('change', dropdown, function (event) {
                    util.update_block(ids, util.FAKE_IDS[event.target.value]);
                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});