/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.details.legalentity_functions',
    dependencies: ['og.common.routes'],
    obj: function () {
        var routes = og.common.routes;
        var render_accounts = function (selector, json) {
                $(selector).html(json.reduce(function (acc, val) {
                    acc.push('<tr><td colspan="2"><a class="og-js-live-anchor" href="',
                             routes.prefix() + routes.hash(og.views.portfolios.rules.load_item, {id: val.portfolio}), '">',
                             val.name,
                             '</a></td></tr>');
                    return acc;
                }, []).join('') || '<tr><td colspan="2">No accounts</td></tr>');
            };

        var render_identifiers = function (selector, identifiers) {
            $(selector).html(identifiers.reduce(function (acc, val) {
                acc.push('<tr><td><span>', val.scheme, '</span></td><td><span>', val.value, '</span></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="3">No external ids</td></tr>');
        };
        var render_attributes = function (selector, attributes) {
            $(selector).html(attributes.reduce(function (acc, val) {
                acc.push('<tr><td><span>', val.name, '</span></td><td><span>', val.value, '</span></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="3">No attributes</td></tr>');
        };
        var render_details = function (selector, details) {
            $(selector).html(details.reduce(function (acc, val) {
                acc.push('<tr><td><span>', val.name, '</span></td><td><span>', val.value, '</span></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="3">No details</td></tr>');
        };
        var render_ratings = function (selector, json) {
            $(selector).html(json.reduce(function (acc, val) {
                acc.push('<tr><td><span>', val.rater, '</span></td><td><span>', val.seniority, '</span></td><td><span>', val.score, '</span></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="3">No ratings</td></tr>');
        };
        var render_capabilities = function (selector, json) {
            $(selector).html(json.reduce(function (acc, val) {
                acc.push('<tr><td colspan="2">', val.name, '</td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="2">No capabilities</td></tr>');
        };
        var render_obligations = function (selector, json) {
            $(selector).html(json.reduce(function (acc, val) {
                acc.push('<tr><td><a class="og-js-live-anchor" href="',
                         routes.prefix() + routes.hash(og.views.securities.rules.load_item, {id: val.oid}), '">',
                         val.obligation,
                         '</a></td><td><a class="og-js-live-anchor" href="',
                         routes.prefix() + routes.hash(og.views.securities.rules.load_item, {id: val.oid}), '">',
                         val.name,
                         '</a></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="2">No issued securities</td></tr>');
        };
        var render_issued_securities = function (selector, json) {
            $(selector).html(json.reduce(function (acc, val) {
                acc.push('<tr><td colspan="2"><a class="og-js-live-anchor" href="',
                         routes.prefix() + routes.hash(og.views.securities.rules.load_item, {id: val.oid}), '">',
                         val.name,
                         '</a></td></tr>');
                return acc;
            }, []).join('') || '<tr><td colspan="2">No issued securities</td></tr>');
        };
        var render_root_portfolio = function (selector, root_portfolio) {
            if (root_portfolio) {
                $(selector).html(['<tr><td colspan="2"><a class="og-js-live-anchor" href="',
                                     routes.prefix() + routes.hash(og.views.portfolios.rules.load_item, {id: root_portfolio}), '">',
                                     root_portfolio,
                                     '</a></td></tr>'].join(''));
            } else {
                $(selector).html('<tr><td colspan="2">No root portfolio</td></tr>');
            }

        };
        return {render_identifiers: render_identifiers, render_details: render_details, render_attributes: render_attributes, render_root_portfolio: render_root_portfolio, render_accounts: render_accounts, render_obligations: render_obligations, render_ratings: render_ratings, render_issued_securities: render_issued_securities, render_capabilities: render_capabilities};
    }
});