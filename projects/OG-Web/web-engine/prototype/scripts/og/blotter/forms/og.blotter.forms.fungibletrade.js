/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fungibletrade',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, data, security, $security_input, util = og.blotter.util, 
            dropdown = '.og-blotter-security-select', bond_future = 'BondFutureSecurity',
            bond_arr = ['CorporateBondSecurity', 'GovernmentBondSecurity','MunicipalBondSecurity'], 
            details_selector = 'og-blocks-fungible-details', ids_selector = 'og-blocks-fungible-security-ids',
            blank_details = "<table class='" + details_selector + "'></div>",
            type_map = {BOND: 1, BOND_FUTURE: 2, EXCHANGE_TRADED: 3};
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {trade: og.blotter.util.fungible_trade};}
            constructor.load = function () {
                constructor.title = 'Fungible Trade';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fungible_tash',
                    data: data,
                    selector: '.OG-blotter-form-block'
                });
                security = new og.blotter.forms.blocks.Security({form: form, label: "Underlying ID", 
                    security: data.trade.securityIdBundle, index: "trade.securityIdBundle"});
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty}),
                    new form.Block({module: 'og.blotter.forms.blocks.fungible_tash', 
                        extras: {quantity: data.trade.quantity},
                        children: [security]
                    }),
                    new form.Block({content: blank_details}),                   
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fungible_security_ids_tash'
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    get_security();
                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data);
                });
                form.on('keyup', security.input_id(), function (event) {
                    get_security();
                });
            }; 
            get_security = function () {
                og.api.rest.blotter.securities.get({id:security.name()}).pipe(
                    function(data){poplate_switch(data);}
                );
            };
            populate_data = function (config){
                var details_block, ids_block;
                switch(config.type){
                    case type_map.BOND:
                        details_block = new form.Block({module: 'og.blotter.forms.blocks.fungible_bond_tash',
                            extras:{issuer: config.inputs.data.issuerName, coupon_type: config.inputs.data.couponType, 
                            coupon_rate: config.inputs.data.couponRate, currency: config.inputs.data.currency}
                        });
                        break;
                    case type_map.BOND_FUTURE:
                        details_block = new form.Block({module: 'og.blotter.forms.blocks.fungible_bond_future_tash',
                            extras:{}
                        });
                        break;
                    case type_map.EXCHANGE_TRADED:
                        details_block = new form.Block({module: 'og.blotter.forms.blocks.fungible_exchange_traded_tash',
                            extras:{}
                        });
                        break;
                }
                details_block.html(function (html){
                    $('.' + details_selector).replaceWith(html);
                });
            };     
            poplate_switch = function (data){
                if(data.error) {clear_info(); return;}
                if(bond_arr.indexOf(data.type)) populate_data({type: type_map.BOND, inputs: data});
                else if(bond_future.indexOf(data.type)) populate_data({type: type_map.BOND_FUTURE, data: data});
                else populate_data({type: type_map.EXCHANGE_TRADED, data: data});
            };
            clear_info = function (){
                $('.' + details_selector).replaceWith(blank_details);
            };
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.submit_new = function () {
                delete data.id;
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});