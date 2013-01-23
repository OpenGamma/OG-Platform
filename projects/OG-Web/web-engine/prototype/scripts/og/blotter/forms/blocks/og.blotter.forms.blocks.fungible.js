/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Fungible',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Fungible = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form;
            form.Block.call(block, 
                {module: 'og.blotter.forms.blocks.fungible_security_tash', 
                extras: {id: id, counterparty: config.counterparty},
                children: [new og.blotter.forms.blocks.Security({
                    form: form, label: "Underlying ID", security: config.underlyingId,
                    index: "trade.securityIdBundle"})]
            });
        };
        Fungible.prototype = new Block(); // inherit Block prototype
        return Fungible;
    }
});