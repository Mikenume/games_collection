// Rutas de la aplicación

import { Routes, Route, Navigate } from 'react-router-dom';

import NavBar from './components/NavBar';
import AdminRoute from './components/AdminRoute';
import Footer from './components/Footer';
import GamesPage from './pages/GamesPage';
import GameDetailPage from './pages/GameDetailPage';
import LoginPage from './pages/LoginPage';
import SettingsPage from './pages/SettingsPage';
import PendingPage from './pages/PendingPage';

export default function App() {
  return (
    <div className="d-flex flex-column min-vh-100">
      <NavBar />
      <main className="container py-4 flex-grow-1">
        <Routes>
          {/* Misma página con dos modos: el key evita que se arrastre el estado de uno a otro */}
          <Route path="/" element={<GamesPage key="catalogo" />} />
          <Route path="/coleccion" element={<AdminRoute><GamesPage key="coleccion" collection /></AdminRoute>} />
          <Route path="/juegos/:id" element={<GameDetailPage />} />
          <Route path="/acceso" element={<LoginPage />} />
          <Route path="/pendientes" element={<AdminRoute allowDemo><PendingPage /></AdminRoute>} />
          <Route path="/ajustes" element={<AdminRoute><SettingsPage /></AdminRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
