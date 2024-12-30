const path = require('path');
const Dotenv = require('dotenv-webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
module.exports = {
    entry: './src/app.js', // Entry point
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'), // Output directory
    },
    resolve: {
        modules: [
            path.resolve(__dirname, 'node_modules'), // Ensure Webpack can find node_modules
            'node_modules',
        ],
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                },
            },
        ],
    },
    mode: 'development',
    devServer: {
        static: './dist', // Folder where your bundle.js is located
        port: 3000, // Optional: Specify the port
        open: true, // Automatically open the browser
    },
    plugins: [
        new Dotenv({
            path: `./.env.${process.env.NODE_ENV}`, // Loads the correct .env file
        }),
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: '**/*.html', // Match all HTML files
                    context: path.resolve(__dirname, 'src'), // Base directory for matching
                    to: path.resolve(__dirname, 'dist'), // Output to `dist` while preserving structure
                },
                // {
                //     from: '**/*.svg', // Match all HTML files
                //     context: path.resolve(__dirname, 'src'), // Base directory for matching
                //     to: path.resolve(__dirname, 'dist'), // Output to `dist` while preserving structure
                // }
            ],
        }),
    ],
};