// Rutas de la aplicación

import { Routes, Route, Navigate } from 'react-router-dom';

import NavBar from './components/NavBar';
import GamesPage from './pages/GamesPage';
import GameDetailPage from './pages/GameDetailPage';
import LoginPage from './pages/LoginPage';
import SettingsPage from './pages/SettingsPage';

export default function App() {
  return (
    <>
      <NavBar />
      <main className="container py-4">
        <Routes>
          <Route path="/" element={<GamesPage />} />
          <Route path="/juegos/:id" element={<GameDetailPage />} />
          <Route path="/acceso" element={<LoginPage />} />
          <Route path="/ajustes" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="container py-4">
        <p className="data-face mb-0">
          Central Videogames · React + Spring Boot + PostgreSQL
        </p>
      </footer>
    </>
  );
}
