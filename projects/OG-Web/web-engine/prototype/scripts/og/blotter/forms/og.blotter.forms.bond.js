/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.Bond',
    dependencies: [],
    obj: function () {   
        return function () {
            var constructor = this, id_block, security_block, 
            id_selector = '.og-blotter-security-id',
            option = Handlebars.compile('<option value="{{{value}}}">{{{name}}}</option>');


            var FAKE_DROPDOWN = [
                {name:'Bond 1', value:'1'},
                {name:'Bond 2', value:'2'},
                {name:'Bond 3', value:'3'},
                {name:'Bond 4', value:'4'}];

            var FAKE_IDS = [
                {bloomberg:'bloomberg 1', ric: 'ric 1', cusip: 'cusip 1', isin: 'isin 1', sedol: 'sedol 1'},
                {bloomberg:'bloomberg 2', ric: 'ric 2', cusip: 'cusip 2', isin: 'isin 2', sedol: 'sedol 2'},
                {bloomberg:'bloomberg 3', ric: 'ric 3', cusip: 'cusip 3', isin: 'isin 3', sedol: 'sedol 3'},
                {bloomberg:'bloomberg 4', ric: 'ric 4', cusip: 'cusip 4', isin: 'isin 4', sedol: 'sedol 4'}];

            var update_block = function (block, extras){
                block.config.extras = extras;
                block.html(function (html) {
                    console.log(html );
                        $(id_selector).html(html);
                    });
            };

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
                    security_block = new form.Block({
                        module: 'og.blotter.forms.blocks.security_tash',
                        extras: {}
                    }),
                    id_block = new form.Block({
                        module: 'og.blotter.forms.blocks.security_ids_tash',
                        extras: {bloomberg:'dafdfsdfs'}
                    }),
                    new og.common.util.ui.Attributes({form: form})
                );
                form.dom();

                form.on('form:load', function () {
                    var $select = $(id_selector);
                    FAKE_DROPDOWN.forEach(function (datum) {
                        var $option = $('<option/>').val(datum.value).html(datum.name);
                        $select.append($option);
                    });
                });

                security_block.on('change', '.og-blotter-security-id', function (event) {
                    update_block(id_block, FAKE_IDS[event.target.value]);
                });
            }; 
            constructor.load();
            constructor.kill = function () {
            };
        };
    }
});