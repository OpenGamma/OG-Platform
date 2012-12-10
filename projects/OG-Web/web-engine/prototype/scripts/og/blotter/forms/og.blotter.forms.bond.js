/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Bond',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, bond = {}, ids = {}, security = {}, util = og.blotter.util, 
            option = Handlebars.compile('<option value="{{{value}}}">{{{name}}}</option>'),
            dropdown = '.og-blotter-security-select';
            bond.selector = '.og-blocks-bond';
            ids.selector = '.og-blocks-security_ids';
            var FAKE_DROPDOWN = [
                {name:'Bond 1', value:'0'},
                {name:'Bond 2', value:'1'},
                {name:'Bond 3', value:'2'},
                {name:'Bond 4', value:'3'}];
            var FAKE_IDS = [
                {bloomberg:'bloomberg 1', ric: 'ric 1', cusip: 'cusip 1', isin: 'isin 1', sedol: 'sedol 1'},
                {bloomberg:'bloomberg 2', ric: 'ric 2', cusip: 'cusip 2', isin: 'isin 2', sedol: 'sedol 2'},
                {bloomberg:'bloomberg 3', ric: 'ric 3', cusip: 'cusip 3', isin: 'isin 3', sedol: 'sedol 3'},
                {bloomberg:'bloomberg 4', ric: 'ric 4', cusip: 'cusip 4', isin: 'isin 4', sedol: 'sedol 4'}];
            var FAKE_BOND = [
                {issuer:'issuer 1',currency: 'currency 1',coupon_type: 'type 1',coupon_rate: 'rate 1',date: 'date 1'},
                {issuer:'issuer 2',currency: 'currency 2',coupon_type: 'type 2',coupon_rate: 'rate 2',date: 'date 2'},
                {issuer:'issuer 3',currency: 'currency 3',coupon_type: 'type 3',coupon_rate: 'rate 3',date: 'date 3'},
                {issuer:'issuer 4',currency: 'currency 4',coupon_type: 'type 4',coupon_rate: 'rate 4',date: 'date 4'}];
            constructor.load = function () {
                constructor.title = 'Bond';
                var form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.bond_tash',
                    data: {},
                    type_map: {},
                    selector: '.OG-blotter-form-block',
                    extras: {}
                });
                form.children.push(
                    security.block = new form.Block({
                        module: 'og.blotter.forms.blocks.security_tash',
                        extras: {}
                    }),
                    bond.block = new form.Block({
                        module: 'og.blotter.forms.blocks.bond_tash',
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
                    FAKE_DROPDOWN.forEach(function (datum) {
                        var $option = $('<option/>').val(datum.value).html(datum.name);
                        $select.append($option);
                    });
                });
                security.block.on('change', dropdown, function (event) {
                    util.update_block(ids, FAKE_IDS[event.target.value]);
                    util.update_block(bond, FAKE_BOND[event.target.value]);
                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});