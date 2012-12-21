/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {
         var FAKE_ATTRIBUTES = [
                {key: 'what',value: 'that'},
                {key: 'colour',value: 'white'},
                {key: 'moral',value: 'bad'},
                {key: 'direction',value: 'down'},
                {key: 'speed',value: 'fast'}];  
        return {
            update_block : function (section, extras){
                section.block.html(function (html) {
                    $(section.selector).html(html);
                }, extras);
            },
            check_radio : function (name, value){
                $('input:radio[name= '+ name +']').filter('[value='+ value + ']').attr('checked', true);
            },
            set_select : function (name, value){
                $('select[name='+ name +']').val(value);
            },
            check_checkbox : function (name, value){
                $('input:checkbox[name= '+ name +']').attr('checked', value);
            },
            
            option : Handlebars.compile('<option value="{{{value}}}">{{{name}}}</option>'),
            FAKE_DROPDOWN : [
                    {name:'Select', value:''},
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
            ],
            FAKE_FX_BARRIER : {
                settlementDate:"21.12.2012",
                barrierLevel: "55.30",
                expiry:"22.12.2012",
                barrierDirection: "Knock In",
                putCurrency: "USD",
                callCurrency: "EEK",
                barrierType: "Up",
                monitoringType: "DISCRETE",
                samplingFrequency: "Friday",
                callAmount: "2.30",
                putAmount: "3.02",
                strike: "55.05",
                longShort: "Short",
                attributes:  FAKE_ATTRIBUTES
            },
            FAKE_FX_OPTION : {
                exerciseType: "Bermudan",
                putCurrency: "USD",
                callCurrency: "EEK",
                callAmount: "2.30",
                putAmount: "3.02",
                longShort: "Short",
                attributes:  FAKE_ATTRIBUTES,
                expiry:"22.12.2012",
                settlementDate:"22.12.2012",
                deliveryInCallCurrency: true
                
            },
            FAKE_CAP_FLOOR : {
                currency: "TOP",
                payer: true,
                cap: false,
                attributes:  FAKE_ATTRIBUTES,
                dayCount: "28/360",
                frequency: "Annual", 
                startDate: "21.12.2012", 
                maturityDate: "22.12.2012", 
                notional: "12",
                strike: "15", 
                longId: "1", 
                shortId: "2", 
                underlyingId: "3"          
            },
            FAKE_FRA : {
                currency: "NOK",
                attributes:  FAKE_ATTRIBUTES,
                startDate: "21.12.2012", 
                endDate: "22.12.2012", 
                fixingDate: "20.12.2012",
                underlyingId: "3",
                amount: "12",
                rate: "0.15"                         
            },
            FAKE_VARIANCE_SWAP : {
                attributes: FAKE_ATTRIBUTES,    
                spotUnderlyingId: "3",
                currency: "EUR",
                strike: "12",
                notional: "15",
                parameterizedAsVariance: true,
                annualizationFactor: "0.14",
                firstObservationDate: "21.12.2012",  
                lastObservationDate: "23.12.2012", 
                settlementDate: "24.12.2012",   
                regionId: "2",
                observationFrequency: "Semi-annual"
            }
        };
    }
});