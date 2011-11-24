/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 * @colordef #fffee5; light yellow for edit mode (on hover)
 */
$.register_module({
    name: 'og.common.util.ui.content_editable',
    dependencies: ['og.common.util.ui', 'og.common.routes', 'og.api.rest'],
    obj: function () {
        var html, editing = false, routes = og.common.routes, ui = og.common.util.ui, api = og.api.rest,
            css_edit = {'background-color': '#fffee5'}, css_not_edit = {'background-color': 'transparent'};
        html = '<div class="og-js-buttons OG-shadow OG-rounded" style="'
             + '        position: absolute; padding: 0; background-color: #eee; border: 1px solid #fff">'
             + '  <div style="white-space: nowrap; display: inline-block; margin: 3px;">'
             + '    <button class="OG-small-button og-button-primary og-js-update">update</button>'
             + '    <button class="OG-small-button og-button-secondary og-js-cancel">cancel</button>'
             + '    <span class="og-js-msg" style="font-size: 12px;"></span>'
             + '  </div>'
             + '</div>'
             + '<input type="text" />';

        return function (config) {
            var attr = config.attribute, handler = config.handler, $attr = $('[' + attr + ']');
            if (typeof attr !== 'string') throw new TypeError(': config.attribute must be a string');
            if (typeof handler !== 'function') throw new TypeError(': config.handler must be a function');
            $attr.css({position: 'relative', 'z-index': '1'});
            $attr.hover(function () {if (!editing) $(this).css(css_edit);}, function () {$(this).css(css_not_edit);});
            $attr.click(function (e) {
                var $this = $(this), $editable_element = $(e.target), cur_content = $this.html(),
                    width = $this.css('width'), font_size = $this.css('font-size'),
                    line_height = $this.css('line-height'),
                    cancel_update = function (error_message) {
                        $editable_element.html(cur_content);
                        editing = false;
                        if (error_message) ui.dialog({type: 'error', message: error_message});
                    },
                    update_field = function () {
                        var current = routes.current(), put_config,
                            new_content = $editable_element.find('input[type=text]').attr('value');
                        if (!new_content) return;
                        put_config = {
                            handler: function (result) {
                                if (result.error) return cancel_update(result.message);
                                $('.og-js-msg').html('saved');
                                ui.message({location: '.OG-details', message: 'saved', css: {'left': '7px'}});
                                handler(e);
                                setTimeout(function () {
                                    $editable_element.html(new_content);
                                    ui.message({location: '.OG-details', destroy: true});
                                    editing = false;
                                }, 250)
                            },
                            id: current.args.id,
                            loading: function () {
                                $('.og-js-msg').html('saving...');
                                ui.message({location: '.OG-details', message: 'saving...', css: {'left': '7px'}});
                            }
                        };
                        put_config[$editable_element.attr(attr)] = new_content;
                        // portfolios also have a node attribute that is necessary, so add if available
                        if (current.args.node) put_config.node = current.args.node;
                        api[current.page.substring(1)].put(put_config);
                };
                /**
                 * Setup input field fit content.
                 * Format container box.
                 */
                if (!editing) {
                    editing = true;
                    $this.css(css_not_edit).html(html).find('input[type=text]').css({
                            'width': parseInt(width) + 10 + 'px',
                            'font-size': font_size,
                            'line-height': line_height,
                            'position': 'relative',
                            'top': '5px',
                            'left': '3px'
                        }).attr('value', cur_content).select();
                    $($editable_element).find('.og-js-buttons').css({
                        'top': '0',
                        'left': '0',
                        'min-width': parseInt(width) + 19 + 'px',
                        'padding-top': parseInt(line_height) + 10 + 'px'
                    });
                }
                $this.find('.og-js-update').click(function (e) {
                    update_field();
                    e.stopImmediatePropagation();
                });
                $this.find('.og-js-cancel').click(function (e) {
                    cancel_update();
                    e.stopImmediatePropagation();
                });
                /**
                 * Keyboard shortcuts
                 */
                $this.keydown(function (e) {
                    if ((e.keyCode === $.ui.keyCode.ENTER) || (e.keyCode === $.ui.keyCode.NUMPAD_ENTER)) update_field();
                    if (e.keyCode === $.ui.keyCode.ESCAPE) cancel_update();
                });
            });

        };

    }
});