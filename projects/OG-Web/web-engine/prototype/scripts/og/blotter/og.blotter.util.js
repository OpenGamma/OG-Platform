/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {

        var bools = {"false": false, "true": true},
            FAKE_ATTRIBUTES = [
                {key: 'what',value: 'that'},
                {key: 'colour',value: 'white'},
                {key: 'moral',value: 'bad'},
                {key: 'direction',value: 'down'},
                {key: 'speed',value: 'fast'}]; 
        return {
            /* Util methods */
            update_block : function (section, extras){
                section.block.html(function (html) {
                    $(section.selector).html(html);
                }, extras);
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
            add_datetimepicker : function (name){
                $('input[name="'+ name +'"]').datetimepicker({
                    dateFormat: 'yy-mm-dd',separator: 'T',firstDay: 1, showTimezone: true, timeFormat: 'hh:mm:ss',
                    timeSuffix: '+00:00[UTC]'
                });
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
                console.log(option);
                if(selection == 'FixedInterestRateLeg')
                    option.attr("disabled", "disabled");
                else
                    option.removeAttr("disabled");
            },
            /* Util data */
            otc_trade : {                
                tradeDate: "2013-01-01",
                premiumCurrency: null,
                tradeTime: "00:00Z",
                premium: null,
                premiumTime: null,
                attributes: {},
                premiumDate: null,
                type: "OtcTrade",
                counterparty: 'ABC Counterparty'
            },
            swap_types : [
                {text:'FLoating Interest Rate Leg', value:'FloatingInterestRateLeg'},
                {text:'FLoating Gearing Interest Rate Leg', value:'FloatingGearingIRLeg'},
                {text:'FLoating Spread Interest Rate Leg', value:'FloatingSpreadIRLeg'},
                {text:'Fixed Interest Rate Leg', value:'FixedInterestRateLeg'}
            ]
        };
    }
});