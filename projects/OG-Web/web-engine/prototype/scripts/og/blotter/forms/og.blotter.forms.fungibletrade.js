/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fungibletrade',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, data, security, $security_input, util = og.blotter.util, promise,
            dropdown = '.og-blotter-security-select', securityId, details_selector = 'og-blocks-fungible-details',
            ids_selector = 'og-blocks-fungible-security-ids',
            blank_details = "<table class='" + details_selector + "'></table>",
            blank_ids = "<table class='" + ids_selector + "'></table>", validate;
            if (config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {trade: og.blotter.util.fungible_trade};}
            if (data.trade.securityIdBundle) securityId = data.trade.securityIdBundle.split(',')[0];
            data.nodeId = config.node ? config.node.id : null;
            constructor.load = function () {
                constructor.title = 'Fungible Trade';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fungible_tash',
                    data: data,
                    selector: '.OG-blotter-form-block',
                    processor: function (data) {util.cleanup(data);}
                });
                security = new og.blotter.forms.blocks.Security({form: form, label: "Underlying ID",
                    security: securityId, index: "trade.securityIdBundle", edit: !!config.details, 
                    callback: function () {get_security();}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade, fungible: true}),
                    new form.Block({module: 'og.blotter.forms.blocks.fungible_tash',
                        extras: {quantity: data.trade.quantity},
                        children: [security]
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    }),
                    new form.Block({content: blank_details}),
                    new form.Block({content: blank_ids})
                );
                form.dom();
                form.on('form:load', function () {
                    util.add_date_picker('.blotter-date');
                    util.add_time_picker('.blotter-time');
                    util.set_initial_focus();
                });
                form.on('form:submit', function (result) {
                    $.when(config.handler(result.data)).then(validate);
                });
            };
            get_security = function () {
                var id = security.name();
                if (id) {
                    promise = og.api.rest.blotter.securities.get({id:id});
                    promise.pipe(function(result){
                        if (promise.id === result.meta.promise) populate(result);
                    });
                }
            };
            populate = function (config){
                var details_block, ids_block, details, basket;
                if(config.error) {clear_info(); return;}
                ids_block = new form.Block({module: 'og.blotter.forms.blocks.fungible_security_ids_tash',
                    extras: {security: security_ids(config.data.externalIdBundle)}
                });
                basket = config.data.basket;
                delete config.data.basket;
                delete config.data.externalIdBundle;
                delete config.data.attributes;
                details = Object.keys(config.data).map(function(key) {
                    return {key: gentrify(key), value:config.data[key]};});
                details_block = new form.Block({module: 'og.blotter.forms.blocks.fungible_details_tash',
                    extras:{detail: details, basket: basket}
                });
                ids_block.html(function (html){
                    $('.' + ids_selector).replaceWith(html);
                });
                details_block.html(function (html){
                    $('.' + details_selector).replaceWith(html);
                });
            };
            gentrify = function (str){
                return str.replace(/([A-Z])/g, ' $1').replace(/^./, function(str){ return str.toUpperCase();});
            };
            security_ids = function (str) {
                return str.split(',').reduce(function(acc, val) {
                    var pair = val.split('~');
                    return acc.concat({'scheme': pair[0].trim(),'id': pair[1].trim()});
                }, []);
            };
            clear_info = function (){
                $('.' + details_selector).replaceWith(blank_details);
                $('.' + ids_selector).replaceWith(blank_ids);
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                delete data.trade.uniqueId;
                delete data.id;
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});