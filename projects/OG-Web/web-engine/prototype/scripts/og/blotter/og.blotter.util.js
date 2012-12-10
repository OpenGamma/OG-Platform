/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {
        return {
            update_block : function (section, extras){
                section.block.html(function (html) {
                    $(section.selector).html(html);
                }, extras);
            },
            option : Handlebars.compile('<option value="{{{value}}}">{{{name}}}</option>'),
            FAKE_DROPDOWN : [
                    {name:'Value 1', value:'0'},
                    {name:'Value 2', value:'1'},
                    {name:'Value 3', value:'2'},
                    {name:'Value 4', value:'3'}
            ],
            FAKE_IDS : [
                {bloomberg:'bloomberg 1', ric: 'ric 1', cusip: 'cusip 1', isin: 'isin 1', sedol: 'sedol 1'},
                {bloomberg:'bloomberg 2', ric: 'ric 2', cusip: 'cusip 2', isin: 'isin 2', sedol: 'sedol 2'},
                {bloomberg:'bloomberg 3', ric: 'ric 3', cusip: 'cusip 3', isin: 'isin 3', sedol: 'sedol 3'},
                {bloomberg:'bloomberg 4', ric: 'ric 4', cusip: 'cusip 4', isin: 'isin 4', sedol: 'sedol 4'}
            ],
            FAKE_BOND : [
                {issuer:'issuer 1',currency: 'currency 1',coupon_type: 'type 1',coupon_rate: 'rate 1',date: 'date 1'},
                {issuer:'issuer 2',currency: 'currency 2',coupon_type: 'type 2',coupon_rate: 'rate 2',date: 'date 2'},
                {issuer:'issuer 3',currency: 'currency 3',coupon_type: 'type 3',coupon_rate: 'rate 3',date: 'date 3'},
                {issuer:'issuer 4',currency: 'currency 4',coupon_type: 'type 4',coupon_rate: 'rate 4',date: 'date 4'}
            ]
        };
    }
});