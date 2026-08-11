/* Mapa de rutas de la aplicación. Cada ruta es una URL y el
   componente de página que se pinta en ella. */

import { Routes, Route, Navigate } from 'react-router-dom';

import NavBar from './components/NavBar';
import GamesPage from './pages/GamesPage';
import GameDetailPage from './pages/GameDetailPage';
import LoginPage from './pages/LoginPage';

export default function App() {
  return (
    <>
      <NavBar />
      <main className="container py-4">
        <Routes>
          <Route path="/" element={<GamesPage />} />
          <Route path="/juegos/:id" element={<GameDetailPage />} />
          <Route path="/acceso" element={<LoginPage />} />
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
