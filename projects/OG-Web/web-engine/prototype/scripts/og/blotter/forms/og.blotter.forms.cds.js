/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.cds',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate, util = og.blotter.util;
            if (config.details) {
                data = config.details.data;
                data.id = config.details.data.trade.uniqueId;
            } else {
                data = {security: {type: config.type, externalIdBundle: "", attributes: {}}, trade: util.otc_trade};
            }
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = config.title;
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.name = util.create_name(data);
                        util.cleanup(data);
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade, name: data.security.name}),
                    new og.blotter.forms.blocks.cds({ form: form, data: data, type: config.type}),
                    new og.common.util.ui.Attributes({ form: form, attributes: data.trade.attributes,
                        index: 'trade.attributes'})
                );
                form.dom();
                form.on('form:load', function () {
                    util.set_initial_focus();
                });
                form.on('form:submit', function (result) {
                    $.when(config.handler(result.data)).then(validate);
                });
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                util.clear_save_as(data);
                form.submit();
            };
        };
    }
});