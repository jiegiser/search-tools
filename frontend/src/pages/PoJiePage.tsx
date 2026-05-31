import { useState, useEffect } from 'react';
import {
  getPoJieResults,
  searchPoJie,
  startPoJieCrawl,
  loginPoJie,
  sendPoJieNotification,
} from '../services/api';
import type { PoJieResource } from '../services/api';

interface PoJiePage {
  content: PoJieResource[];
  totalElements: number;
  totalPages: number;
  number: number;
}

function PoJiePage() {
  const [activeTab, setActiveTab] = useState<'results' | 'search' | 'login'>('results');
  const [results, setResults] = useState<PoJiePage | null>(null);
  const [searchResults, setSearchResults] = useState<PoJiePage | null>(null);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [crawlMessage, setCrawlMessage] = useState<string | null>(null);

  // 登录表单
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loginMessage, setLoginMessage] = useState<string | null>(null);

  useEffect(() => {
    if (activeTab === 'results') {
      loadResults(0);
    }
  }, [activeTab]);

  const loadResults = async (p: number) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getPoJieResults(p, 20);
      setResults(data);
      setPage(p);
    } catch (err: any) {
      setError(err.response?.data?.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyword.trim()) return;

    setLoading(true);
    setError(null);
    try {
      const data = await searchPoJie(keyword, 0, 20);
      setSearchResults(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '搜索失败');
    } finally {
      setLoading(false);
    }
  };

  const handleStartCrawl = async () => {
    setLoading(true);
    setCrawlMessage(null);
    setError(null);
    try {
      const data = await startPoJieCrawl();
      setCrawlMessage(`爬取完成，获取 ${data.resourceCount} 个资源`);
      loadResults(0);
    } catch (err: any) {
      setError(err.response?.data?.message || '爬取失败');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;

    setLoading(true);
    setLoginMessage(null);
    setError(null);
    try {
      const data = await loginPoJie(username, password);
      setLoginMessage(data.message);
    } catch (err: any) {
      setError(err.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSendNotification = async () => {
    setLoading(true);
    try {
      const data = await sendPoJieNotification();
      setCrawlMessage(data.message);
    } catch (err: any) {
      setError(err.response?.data?.message || '发送通知失败');
    } finally {
      setLoading(false);
    }
  };

  const renderResourceCard = (resource: PoJieResource) => (
    <div key={resource.id} className="resource-card">
      <a
        className="resource-title"
        href={resource.threadUrl}
        target="_blank"
        rel="noopener noreferrer"
      >
        {resource.title}
      </a>
      <div className="resource-meta">
        {resource.panUrl && (
          <span className="meta-item">
            <a
              href={resource.panUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: '#1890ff', textDecoration: 'none' }}
            >
              打开网盘链接
            </a>
          </span>
        )}
        {resource.panType && (
          <span className="meta-item">
            <span className={`pan-badge pan-${resource.panType}`}>
              {resource.panType}
            </span>
          </span>
        )}
        {resource.extractCode && (
          <span className="meta-item">
            提取码: <strong>{resource.extractCode}</strong>
          </span>
        )}
        {resource.author && (
          <span className="meta-item">作者: {resource.author}</span>
        )}
        {resource.categoryName && (
          <span className="meta-item">分类: {resource.categoryName}</span>
        )}
        {resource.replyCount !== undefined && (
          <span className="meta-item">回复: {resource.replyCount}</span>
        )}
        <span className="meta-item">
          {resource.notified ? '已通知' : '未通知'}
        </span>
      </div>
    </div>
  );

  const currentResults = activeTab === 'search' ? searchResults : results;
  const totalPages = currentResults ? currentResults.totalPages : 0;

  return (
    <div className="page-container">
      <div className="page-header">
        <h2>吾爱破解资源</h2>
        <p className="page-desc">52pojie.cn 论坛网盘资源爬取与管理</p>
      </div>

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'results' ? 'active' : ''}`}
          onClick={() => setActiveTab('results')}
        >
          资源列表
        </button>
        <button
          className={`tab ${activeTab === 'search' ? 'active' : ''}`}
          onClick={() => setActiveTab('search')}
        >
          搜索资源
        </button>
        <button
          className={`tab ${activeTab === 'login' ? 'active' : ''}`}
          onClick={() => setActiveTab('login')}
        >
          登录设置
        </button>
      </div>

      {/* 操作栏 */}
      <div style={{ margin: '16px 0', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <button
          className="btn btn-primary"
          onClick={handleStartCrawl}
          disabled={loading}
          style={{ padding: '8px 16px', borderRadius: '4px', border: 'none', background: '#1890ff', color: '#fff', cursor: 'pointer' }}
        >
          {loading ? '爬取中...' : '手动爬取52pojie'}
        </button>
        <button
          className="btn btn-secondary"
          onClick={handleSendNotification}
          disabled={loading}
          style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #d9d9d9', background: '#fff', cursor: 'pointer' }}
        >
          发送通知邮件
        </button>
        <button
          className="btn btn-secondary"
          onClick={() => loadResults(0)}
          disabled={loading}
          style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #d9d9d9', background: '#fff', cursor: 'pointer' }}
        >
          刷新列表
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {crawlMessage && (
        <div style={{ padding: '10px', background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: '4px', marginBottom: '16px', color: '#52c41a' }}>
          {crawlMessage}
        </div>
      )}

      {/* 资源列表Tab */}
      {activeTab === 'results' && (
        <div>
          {loading ? (
            <div className="loading">
              <div className="spinner"></div>
              <p>加载中...</p>
            </div>
          ) : currentResults && currentResults.content.length > 0 ? (
            <>
              <div className="result-stats" style={{ marginBottom: '16px', color: '#666' }}>
                共 {currentResults.totalElements} 个资源
              </div>
              {currentResults.content.map(renderResourceCard)}
              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    className="page-button"
                    disabled={page === 0}
                    onClick={() => loadResults(page - 1)}
                  >
                    上一页
                  </button>
                  <span className="page-info">
                    第 {page + 1} / {totalPages} 页
                  </span>
                  <button
                    className="page-button"
                    disabled={page >= totalPages - 1}
                    onClick={() => loadResults(page + 1)}
                  >
                    下一页
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="no-results">
              <h3>暂无52pojie资源</h3>
              <p>点击"手动爬取52pojie"获取资源，或先在"登录设置"中登录</p>
            </div>
          )}
        </div>
      )}

      {/* 搜索Tab */}
      {activeTab === 'search' && (
        <div>
          <form onSubmit={handleSearch} className="search-box" style={{ marginBottom: '20px' }}>
            <input
              type="text"
              className="search-input"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="搜索52pojie资源..."
            />
            <button type="submit" className="search-button" disabled={loading}>
              {loading ? '搜索中...' : '搜索'}
            </button>
          </form>

          {searchResults && searchResults.content.length > 0 ? (
            <>
              <div className="result-stats" style={{ marginBottom: '16px', color: '#666' }}>
                找到 {searchResults.totalElements} 个资源
              </div>
              {searchResults.content.map(renderResourceCard)}
            </>
          ) : searchResults ? (
            <div className="no-results">
              <h3>未找到相关资源</h3>
              <p>请尝试其他关键词</p>
            </div>
          ) : null}
        </div>
      )}

      {/* 登录Tab */}
      {activeTab === 'login' && (
        <div style={{ maxWidth: '400px' }}>
          <h3 style={{ marginBottom: '16px' }}>登录52pojie</h3>
          <p style={{ color: '#666', marginBottom: '20px', fontSize: '14px' }}>
            登录后可以爬取更多资源内容。请使用52pojie.cn的账号密码。
          </p>
          <form onSubmit={handleLogin}>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500' }}>用户名</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="52pojie用户名"
                style={{ width: '100%', padding: '8px 12px', border: '1px solid #d9d9d9', borderRadius: '4px', fontSize: '14px' }}
              />
            </div>
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', marginBottom: '6px', fontWeight: '500' }}>密码</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="52pojie密码"
                style={{ width: '100%', padding: '8px 12px', border: '1px solid #d9d9d9', borderRadius: '4px', fontSize: '14px' }}
              />
            </div>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
              style={{ padding: '10px 24px', borderRadius: '4px', border: 'none', background: '#1890ff', color: '#fff', cursor: 'pointer', fontSize: '14px' }}
            >
              {loading ? '登录中...' : '登录'}
            </button>
          </form>

          {loginMessage && (
            <div style={{ marginTop: '16px', padding: '10px', background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: '4px', color: '#52c41a' }}>
              {loginMessage}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default PoJiePage;
