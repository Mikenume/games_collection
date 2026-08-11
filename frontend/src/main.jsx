/* Punto de entrada. Aquí se monta React en el <div id="root">
   del index.html y se envuelve la app en los dos "proveedores"
   que necesita: el router (URLs) y la sesión. */

import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css';

import { AuthProvider } from './auth/AuthContext';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
