const path = require('path');
const webpack = require('webpack');

module.exports = {
    mode: 'production',

    entry: {
        main: './src/main.js',
        vendor: ['three']
    },
    optimization: {
        splitChunks: {
            cacheGroups: {
                commons: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendor',
                    chunks: 'all'
                }
            }
        }
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist')
    }
};
