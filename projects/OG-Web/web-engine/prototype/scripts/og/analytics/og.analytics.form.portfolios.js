/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.Portfolios',
    dependencies: ['og.common.util.ui.Dropdown'],
    obj: function () {
        var module = this, menu, Block = og.common.util.ui.Block;
        var Portfolios = function (config) {
            var block = this, form = config.form, index = 'portfolio', title = 'portfolio';
            form.Block.call(block, {
                template: '{{{children}}}<div class="OG-icon og-icon-down"></div>',
                children: [new og.common.util.ui.Dropdown({
                    form: form, resource: 'portfolios', index: index, value: config.val,
                    rest_options: {page: '*'}, placeholder: 'Select...', fields: [0, 2]
                })],
                processor: function (data) {
                    if (!data[index]) // hack to get the value of a searchable dropdown
                        data[index] = $('#' + form.id + ' select[name=' + index + ']').siblings('select').val();
                }
            });
            block.on('form:load', function () {
                var select = $('#' + form.id + ' select[name=' + index + ']').searchable().focus().hide()
                select.siblings('input').removeAttr('style').show().blur(function () {
                    $(this).show();
                    select.siblings('select').hide();
                }).keydown(function (event) {
                    if (event.keyCode === 38 || event.keyCode === 40) select.siblings('select').show();
                    $(this).show();
                }).click(function (event) { $(this).show() });
                select.parent().siblings('.og-icon-down').click(function () {
                     select.siblings('select').show();
                     select.siblings('input').focus();
                })
                select.siblings('select').click(function (event) {
                    select.siblings('input').show().val($(event.target).text());
                })
            });
        };
        Portfolios.prototype = new Block;
        return Portfolios;
    }
});