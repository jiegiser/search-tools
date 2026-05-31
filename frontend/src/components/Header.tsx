import { Link, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getSystemStatus } from '../services/api';
import type { SystemStatus } from '../services/api';

function Header() {
  const location = useLocation();
  const [status, setStatus] = useState<SystemStatus | null>(null);

  useEffect(() => {
    loadStatus();
  }, []);

  const loadStatus = async () => {
    try {
      const data = await getSystemStatus();
      setStatus(data);
    } catch (err) {
      console.error('Failed to load status:', err);
    }
  };

  const isActive = (path: string) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <header className="header">
      <div className="header-content">
        <div className="header-left">
          <Link to="/" className="logo">
            <span className="logo-icon">💾</span>
            <span className="logo-text">网盘搜索</span>
          </Link>
          <nav className="nav">
            <Link to="/" className={`nav-link ${isActive('/')}`}>
              搜索
            </Link>
            <Link to="/crawl" className={`nav-link ${isActive('/crawl')}`}>
              爬取
            </Link>
            <Link to="/pojie" className={`nav-link ${isActive('/pojie')}`}>
              52pojie
            </Link>
            <Link to="/admin" className={`nav-link ${isActive('/admin')}`}>
              管理
            </Link>
          </nav>
        </div>
        {status && (
          <div className="header-stats">
            <span className="stat">资源: {status.totalResources}</span>
            <span className="stat">今日: {status.todayResources}</span>
          </div>
        )}
      </div>
    </header>
  );
}

export default Header;
