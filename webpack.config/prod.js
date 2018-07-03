const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'production',

    entry: {
        main: './src/main.ts',
        vendor: ['pixi.js']
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
            },
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            }
        ]
    },

    resolve: {
        extensions: ['.tsx', '.ts', '.js']
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
        new HtmlWebpackPlugin({
            hash: true,
            title: 'Origin',
            template: "./src/index.html",
            filename: '../dist/index.html'
        })
    ],

    output: {
        filename: '[name].bundle.js',
        path: path.resolve(path.join(__dirname, "..", "dist")),
    }
};
