import path from 'path';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default {
  entry: './src/main.tsx',
  output: {
    path: path.resolve(__dirname, '../backend/src/main/resources/static'),
    filename: 'bundle.js',
    clean: true,
    publicPath: '/',
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './index.html',
      filename: 'index.html',
      inject: true,
    }),
  ],
  devServer: {
    static: {
      directory: path.join(__dirname, '../backend/src/main/resources/static'),
    },
    port: 4396,
    hot: true,
    historyApiFallback: true,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          const errorMessage = error.message || '';
          if (errorMessage.includes('ResizeObserver loop completed with undelivered notifications')) {
            return false;
          }
          return true;
        },
      },
    },
    proxy: {
      '/dialingtest/api': {
        target: 'https://localhost:8087',
        changeOrigin: true,
        secure: false,
      },
    },
  },
};
