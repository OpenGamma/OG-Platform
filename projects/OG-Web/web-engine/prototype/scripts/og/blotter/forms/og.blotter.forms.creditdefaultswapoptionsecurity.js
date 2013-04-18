/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.creditdefaultswapoptionsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util, cds_select,
            cds_id = 'blotter-cds-block', prefix = 'underlying';
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: config.type, externalIdBundle: "", attributes: {}}, 
                trade: util.otc_trade};}
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'CDS Option';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.cds_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.name = util.create_name(data);
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade}),
                    new ui.Dropdown({
                        form: form, resource: 'blotter.exercisetypes', index: 'security.exerciseType',
                        value: data.security.exerciseType, placeholder: 'Select Exercise Type'
                    }),
                    cds_select = new ui.Dropdown({
                        form: form, placeholder: 'Select CDS Type',
                        data_generator: function (handler) {handler(og.blotter.util.cds_types);}
                    }),
                    cds_block = new form.Block({content:"<div id='" + cds_id + "'></div>"}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                    if (data.underlying) {
                        var $cds_select = $('#' + cds_select.id), type = data.underlying.type.toLowerCase();
                        swap_cds({type: type});
                        $cds_select.val(type);
                    }
                    if (data.security.length) return;

                });
                form.on('form:submit', function (result){
                    $.when(config.handler(result.data)).then(validate);
                });
                form.on('change', '#' + cds_select.id, function (event) {
                    swap_cds({type: event.target.value});
                });

            };
            swap_cds = function (cds) {
                var new_block;
                if(!cds.type.length) {
                    new_block = new form.Block({content:"<div id='" + cds_id + "'></div>"});
                } else {
                    var standard = ~['standardfixedrecoverycdssecurity', 
                            'standardrecoverylockcdssecurity'].indexOf(cds.type),
                        legacy = ~['legacyfixedrecoverycdssecurity', 'legacyrecoverylockcdssecurity', 
                            'legacyvanillacdssecurity'].indexOf(cds.type),
                        stdvanilla = ~cds.type.indexOf('standardvanillacdssecurity'), 
                        index = ~cds.type.indexOf('creditdefaultswapindexsecurity'); 
                    new_block = new og.blotter.forms.blocks.cds({form: form, data: data, standard: standard, 
                        stdvanilla: stdvanilla, legacy: legacy, index: index, prefix: prefix});
                }
                new_block.html(function (html) {
                    $('#' + cds_id).replaceWith(html);
                    util.set_cds_data(prefix, data);
                });
                form.children[2] = new_block;
                
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                delete data.id;
                form.submit();
            };
        };
    }
});