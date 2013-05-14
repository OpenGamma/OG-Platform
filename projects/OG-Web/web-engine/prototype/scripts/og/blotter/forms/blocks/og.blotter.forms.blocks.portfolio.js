/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Portfolio',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Portfolio = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form;
            form.Block.call(block, {module: 'og.blotter.forms.blocks.portfolio_tash',
                extras: {id: id, counterparty: config.counterparty, node: config.node, trade: config.trade,
                    name: config.name, fungible: config.fungible},
                children: [
                    new form.Block({module:'og.views.forms.currency_tash', extras:{name: 'trade.premiumCurrency'}})
                ]
            });
            form.on('form:load', function () {
                $('.premium-toggle', '#'+id).click(function (event) {
                    var $hidden = $('.hidden', '#' + id);
                    $hidden.is(':visible') ? $hidden.hide() : $hidden.show();
                    event.preventDefault();
                    return false;
                });
                og.blotter.util.set_select("trade.premiumCurrency", config.trade.premiumCurrency);
            });
        };
        Portfolio.prototype = new Block(); // inherit Block prototype
        return Portfolio;
    }
});