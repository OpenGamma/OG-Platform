/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

$.register_module({
    name: 'og.common.util.ui.Block',
    dependencies: ['og.api.text'],
    obj: function () {
        var module = this, api_text = og.api.text;
        /**
         * creates a Block instance
         * @name Block
         * @constructor
         * @param {Form} form parent Form instance
         * @param {Object} config configuation options for Block instance
         * @param {String} config.module the Handlebars template name (optional)
         * @param {String} config.content static content that will populate the Block (optional)
         * @param {Function} config.generator function that will receive a handler into which it can send Block contents
         * whenever it is ready, useful for populating a block with async content (optional)
         * @param {Object} config.extras values plugged into the Handlerbars template (optional)
         * @param {String} config.template simple Handlebars template, an HTML blob called "children"
         * @property {Array} children collection of children within a Block instance, each child is itself a Block
         */
        var Block = function (form, config) {
            var block = this, config = block.config = config || {};
            block.children = config.children || [];
            block.form = form;
            if (config.module) api_text({module: config.module, loading: function () {block.template = 'loading';}})
                .pipe(function (html) {
                    block.template = Handlebars.compile(html);
                    if (block.ready_handler) block.html(block.ready_handler);
                });
            else if (config.template) block.template = Handlebars.compile(config.template);
        };
        /**
         * generates the HTML for a Block instance and all of its children and calls a supplied handler with that string
         * @param {Function} handler the handler that receives the HTML string
         * @param {Object} extras key/value pairs that will override the original config.extras
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.html = function (handler, extras) {
            var block = this, template = block.template, total = block.children.length, done = 0, result = [],
                generator = block.config.generator;
            block.ready_handler = null;
            /** @private */
            var internal_handler = function () {
                var template_data = $.extend(
                    result.reduce(function (acc, val, idx) {return acc['item_' + idx] = val, acc;}, {
                        children: result.join('') // all children can be populated into a special field called children
                    }), block.config.extras, extras || {}
                );
                return generator ? generator(handler, template, template_data) : handler(template(template_data));
            };
            if (block.config.content) return handler(block.config.content), block;
            if (template === 'loading') // set a local handler and wait for template to return
                return (block.ready_handler = handler), block;
            if (!total) return internal_handler(), block;
            block.children.forEach(function (child, index) {
                child.html(function (html) {(result[index] = html), (total === ++done) && internal_handler();});
            });
            return block;
        };
        /**
         * simulates a form:load event, only used when new blocks are added and need to be set up
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.load = function () {
            var block = this, form = block.form;
            (form['og.common.events']['form:load'] || []) // mimic a form load event
                .forEach(function (val) {if (val.origin === block) val.handler();});
            block.children.forEach(function (child) {child.load();});
            return block;
        };
        /**
         * delegates off functionality to current Block instance's parent {@link Form#off}
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.off = function () {
            var block = this;
            return block.form ? block.form.off.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        /**
         * delegates on functionality to current Block instance's parent {@link Form#on}
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.on = function () {
            var block = this;
            return block.form ? block.form.on.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        /** @ignore */
        Block.prototype.process = function (data, errors) {
            var block = this, processor = block.config.processor;
            block.children.forEach(function (child) {child.process(data, errors);});
            try {if (processor) processor(data);} catch (error) {errors.push(error);}
        };
        Block.prototype.template = Handlebars.compile('{{{children}}}');
        return Block;
    }
});