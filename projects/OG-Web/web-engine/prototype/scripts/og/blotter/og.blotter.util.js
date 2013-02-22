/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {
        var bools = {"false": false, "true": true};
        return {
            /* Util methods */
            create_name : function (data){
                return data.security.type + " " + data.trade.tradeDate;
            },
            check_radio : function (name, value){
                $('input:radio[name="'+ name +'"]').filter('[value='+ value + ']').attr('checked', true);
            },
            set_select : function (name, value){
                $('select[name="'+ name +'"]').val(value);
            },
            check_checkbox : function (name, value){
                $('input:checkbox[name="'+ name +'"]').prop('checked', bools[value]);
            },
            add_date_picker : function (selector){
                $(selector).datepicker({ dateFormat: 'yy-mm-dd', changeMonth: true, changeYear: true });
            },
            add_time_picker : function (selector) {
                $(selector).datetimepicker({ timeOnly:true });
            },
            get_checkbox : function (name) {
                return $('input:checkbox[name="'+ name +'"]').is(':checked').toString();
            },
            set_datetime : function (name, value){
                $('input[name="'+ name +'"]').datetimepicker('setDate', value);
            },
            get_attributes : function () {
                var attributes = {};
                $('.og-attributes-add-list li').each(function (i, elm) {
                    var arr = $(elm).text().split(' = ');
                    attributes[arr[0]] = arr[1];
                });
                return attributes;
            },
            toggle_fixed : function (ele, selection) {
                var option = ele.find("option[value='FixedInterestRateLeg']");
                if(selection == 'FixedInterestRateLeg')
                    option.attr("disabled", "disabled");
                else
                    option.removeAttr("disabled");
            },
            /* Util data */
            otc_trade : {
                attributes: {},
                type: "OtcTrade",
                premiumCurrency: 'USD',
                premium: 10

            },
            fungible_trade : {
                attributes: {},
                type: "FungibleTrade",
                premiumCurrency: 'USD',
                premium: 10
            },
            swap_types : [
                {text:'Floating Interest Rate Leg', value:'FloatingInterestRateLeg'},
                {text:'Floating Gearing Interest Rate Leg', value:'FloatingGearingIRLeg'},
                {text:'Floating Spread Interest Rate Leg', value:'FloatingSpreadIRLeg'},
                {text:'Fixed Interest Rate Leg', value:'FixedInterestRateLeg'}
            ]
        };
    }
});