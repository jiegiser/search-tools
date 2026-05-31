import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import SearchPage from './pages/SearchPage';
import ResourceDetail from './pages/ResourceDetail';
import CrawlPage from './pages/CrawlPage';
import AdminPage from './pages/AdminPage';
import PoJiePage from './pages/PoJiePage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <Header />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<SearchPage />} />
            <Route path="/resource/:id" element={<ResourceDetail />} />
            <Route path="/crawl" element={<CrawlPage />} />
            <Route path="/pojie" element={<PoJiePage />} />
            <Route path="/admin" element={<AdminPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App
