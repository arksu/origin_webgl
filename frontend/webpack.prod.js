const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
    mode: 'production',
    entry: {
        app: {
            import: './src/main.ts',
            dependOn: ['pixi']
        },
        fontawesome: './src/fontawesome.ts',
        pixi: 'pixi.js',
        axios: 'axios',
    },
    output: {
        filename: '[name].[contenthash:8].js',
    },
});