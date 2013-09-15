/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {
        var util = this, bools = {"false": false, "true": true};
        util = {
            /* Util methods */
            create_name : function (data) {
                if (data.security.name) return data.security.name;
                else return data.security.type + " " + data.trade.tradeDate;
            },
            create_underlying_name : function (data) {
                if (data.underlying.name) return data.underlying.name;
                else return data.underlying.type + " " + data.trade.tradeDate;
            },
            create_cds_name : function (data) {
                if (data.security.name)  return data.security.name;
                else return data.security.type + " on " + data.underlying.type + " " + data.trade.tradeDate;
            },
            clear_save_as : function (data) {
                delete data.id;
                delete data.security.uniqueId;
                delete data.trade.uniqueId;
                delete data.security.name;
            },
            clear_underlying_save_as : function (data) {
                delete data.security.underlyingId;
                delete data.underlying.uniqueId;
                delete data.underlying.name;
                delete data.underlying.externalIdBundle;
            },
            get_unique_id : function (input) {
                var arr = input.split('~');
                return arr[0] + '~' + arr[1];
            },
            check_radio : function (name, value) {
                $('input:radio[name="' + name + '"]').filter('[value=' + value + ']').attr('checked', true);
            },
            set_select : function (name, value) {
                $('select[name="' + name + '"]').val(value);
            },
            check_checkbox : function (name, value) {
                $('input:checkbox[name="' + name + '"]').prop('checked', bools[value]);
            },
            add_date_picker : function (selector) {
                $(selector).datepicker({ dateFormat: 'yy-mm-dd', changeMonth: true, changeYear: true });
            },
            add_time_picker : function (selector) {
                $(selector).datetimepicker({ timeOnly: true });
            },
            get_checkbox : function (name) {
                return $('input:checkbox[name="' + name + '"]').is(':checked').toString();
            },
            get_attributes : function () {
                var attributes = {};
                $('.og-attributes-add-list li').each(function (i, elm) {
                    var arr = $(elm).text().split(' = ');
                    attributes[arr[0]] = arr[1];
                });
                return attributes;
            },
            cleanup : function (obj) {
                Object.keys(obj).forEach(function (key) {
                    var value = obj[key];
                    if (typeof value === 'string' && !value.length) {
                        delete obj[key];
                    } else if (value instanceof Object) {
                        util.cleanup(value);
                    }
                });
            },
            set_initial_focus : function () {
                $('input[name="trade.counterparty"]').focus();
            },
            set_cds_data : function (prefix, data) {
                util.add_date_picker('.blotter-date');
                util.add_time_picker('.blotter-time');
                if (data[prefix].length) {
                    return;
                }
                util.check_radio(prefix + '.buy', data[prefix].buy);
                util.check_checkbox(prefix + '.protectionStart', data[prefix].protectionStart);
                util.check_checkbox(prefix + '.includeAccruedPremium', data[prefix].includeAccruedPremium);
                util.check_checkbox(prefix + '.adjustEffectiveDate', data[prefix].adjustEffectiveDate);
                util.check_checkbox(prefix + '.adjustCashSettlementDate', data[prefix].adjustCashSettlementDate);
                util.check_checkbox(prefix + '.adjustMaturityDate', data[prefix].adjustMaturityDate);
                util.check_checkbox(prefix + '.immAdjustMaturityDate', data[prefix].immAdjustMaturityDate);
                util.check_checkbox(prefix + '.adjustSettlementDate', data[prefix].adjustSettlementDate);
                if (data[prefix].notional) {
                    util.set_select(prefix + '.notional.currency', data[prefix].notional.currency);
                }
                if (data[prefix].upfrontPayment) {
                    util.set_select(prefix + '.upfrontPayment.currency', data[prefix].upfrontPayment.currency);
                }
                if (data[prefix].upfrontAmount) {
                    util.set_select(prefix + '.upfrontAmount.currency', data[prefix].upfrontAmount.currency);
                }
            },
            is_standard_cds : function (type) {
                return ~['StandardFixedRecoveryCDSSecurity',
                         'StandardVanillaCDSSecurity',
                         'StandardRecoveryLockCDSSecurity'].indexOf(type);
            },
            is_legacy_cds : function (type) {
                return ~['LegacyFixedRecoveryCDSSecurity',
                         'LegacyRecoveryLockCDSSecurity',
                         'LegacyVanillaCDSSecurity'].indexOf(type);
            },
            is_recrate_cds : function (type) {
                return ~['LegacyFixedRecoveryCDSSecurity',
                         'LegacyRecoveryLockCDSSecurity',
                         'StandardRecoveryLockCDSSecurity',
                         'StandardFixedRecoveryCDSSecurity'].indexOf(type);
            },
            is_stdvanilla_cds : function (type) {
                return ~type.indexOf('StandardVanillaCDSSecurity');
            },
            is_index_cds : function (type) {
                return ~type.indexOf('CreditDefaultSwapIndexSecurity');
            },
            /* Util data */
            otc_trade : {
                attributes: {},
                type: "OtcTrade"
            },
            fungible_trade : {
                attributes: {},
                type: "FungibleTrade"
            },
            swap_types : [
                {text: 'Floating Interest Rate Leg', value: 'FloatingInterestRateLeg'},
                {text: 'Floating Gearing Interest Rate Leg', value: 'FloatingGearingIRLeg'},
                {text: 'Floating Spread Interest Rate Leg', value: 'FloatingSpreadIRLeg'},
                {text: 'Fixed Interest Rate Leg', value: 'FixedInterestRateLeg'}
            ],
            inflation_swap_types : [
                {text: 'Fixed Inflation Leg', value: 'FixedInflationSwapLeg'},
                {text: 'Inflation Index Leg', value: 'InflationIndexSwapLeg'},
            ],
            cds_types : [
                {text: 'CDS Index', value: 'CreditDefaultSwapIndexSecurity'},
                {text: 'Legacy Fixed Recovery CDS', value: 'LegacyFixedRecoveryCDSSecurity'},
                {text: 'Legacy Recovery Lock CDS', value: 'LegacyRecoveryLockCDSSecurity'},
                {text: 'Legacy Vanilla CDS', value: 'LegacyVanillaCDSSecurity'},
                {text: 'Standard Fixed Recovery CDS', value: 'StandardFixedRecoveryCDSSecurity'},
                {text: 'Standard Recovery Lock CDS', value: 'StandardRecoveryLockCDSSecurity'},
                {text: 'Standard Vanilla CDS', value: 'StandardVanillaCDSSecurity'}
            ]
        };
        return util;
    }
});
