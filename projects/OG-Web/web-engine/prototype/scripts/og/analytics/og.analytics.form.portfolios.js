/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.Portfolios',
    dependencies: ['og.common.util.ui.Dropdown'],
    obj: function () {
        var Block = og.common.util.ui.Block;
        var Portfolios = function (config) {
            var block = this, form = config.form, index = 'portfolio', selectedIndex;
            form.Block.call(block, {
                template: '{{{children}}}<div class="OG-icon og-icon-down"></div>',
                children: [new og.common.util.ui.Dropdown({
                    form: form, resource: 'portfolios', index: index, value: config.val,
                    rest_options: {page: '*'}, placeholder: 'Select portfolio', fields: [0, 2]
                })],
                processor: function (data) {
                    if (!data[index]) // hack to get the value of a searchable dropdown
                        data[index] = $('#' + form.id + ' select[name=' + index + ']').siblings('select').val();
                }
            });
            block.on('form:load', function () {
                var select = $('#' + form.id + ' select[name=' + index + ']').searchable().focus().hide(),selectedIndex,
                    list = select.siblings('select').addClass('dropdown-list'),
                    input = select.siblings('input').attr('placeholder', 'Select portfolio'),
                    toggle = select.parent().siblings('.og-icon-down');

                input.removeAttr('style').show().blur(function () {
                    $(this).show();
                    list.hide();
                }).keydown(function (event) {
                    if (event.keyCode === 38 || event.keyCode === 40) list.show();
                    $(this).show();
                }).click(function (event) {
                    if (input.val() !== '') {
                        list.show().prop('selectedIndex', selectedIndex);
                        select.prop('selectedIndex', selectedIndex);
                    }
                    $(this).show();
                });

                toggle.click(function (event) {
                    if (input.val() !== '') {
                        select.prop('selectedIndex', selectedIndex);
                        list.show().prop('selectedIndex', selectedIndex);
                    }
                    input.focus(0).trigger('keydown');
                });

                list.on('mousedown', function (event) {
                    var text = $(event.target).text(),
                        selectedIndex = $(this).prop('selectedIndex');
                    select.prop('selectedIndex', selectedIndex);
                    list.prop('selectedIndex', selectedIndex);
                    input.show().val(text).focus(0);
                });
            });
        };
        Portfolios.prototype = new Block;
        return Portfolios;
    }
});