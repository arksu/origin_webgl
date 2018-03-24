const path = require('path');
const BrowserSyncPlugin = require('browser-sync-webpack-plugin');

module.exports = {
    mode: 'development',

    entry: {
        main: './src/main.js',
        vendor: ['three']
    },

    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                include: path.join(__dirname, 'src'),
                use: {
                    loader: "babel-loader"
                }
            }
        ]
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

    plugins: [
        new BrowserSyncPlugin({
            host: 'localhost',
            port: 3000,
            server: {baseDir: ['./', 'dist']}
        })
    ],

    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist')
    }
};
