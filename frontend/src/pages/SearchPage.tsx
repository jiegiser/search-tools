import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { search, getHotKeywords } from '../services/api';
import type { SearchResult } from '../services/api';

function SearchPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('q') || '');
  const [results, setResults] = useState<SearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hotKeywords, setHotKeywords] = useState<string[]>([]);
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadHotKeywords();
    const query = searchParams.get('q');
    if (query) {
      setKeyword(query);
      performSearch(query, 0);
    }
  }, []);

  const loadHotKeywords = async () => {
    try {
      const keywords = await getHotKeywords(10);
      setHotKeywords(keywords);
    } catch (err) {
      console.error('Failed to load hot keywords:', err);
    }
  };

  const performSearch = async (query: string, pageNum: number) => {
    if (!query.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const result = await search(query, pageNum);
      setResults(result);
      setPage(pageNum);
    } catch (err: any) {
      setError(err.response?.data?.message || '搜索失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      setSearchParams({ q: keyword });
      performSearch(keyword, 0);
    }
  };

  const handleKeywordClick = (kw: string) => {
    setKeyword(kw);
    setSearchParams({ q: kw });
    performSearch(kw, 0);
  };

  const handlePageChange = (newPage: number) => {
    performSearch(keyword, newPage);
    window.scrollTo(0, 0);
  };

  const handleResourceClick = (id: number) => {
    navigate(`/resource/${id}`);
  };

  const renderSearchResults = () => {
    if (!results) return null;

    const totalPages = Math.ceil(results.totalCount / results.pageSize);

    return (
      <div className="search-results">
        <div className="result-header">
          <h2>搜索结果</h2>
          <div className="result-stats">
            找到 {results.totalCount} 个相关资源，耗时 {results.duration} 毫秒
          </div>
        </div>

        {results.resources.length === 0 ? (
          <div className="no-results">
            <h3>未找到相关资源</h3>
            <p>请尝试其他关键词</p>
          </div>
        ) : (
          <>
            {results.resources.map((resource) => (
              <div
                key={resource.id}
                className="resource-card"
                onClick={() => handleResourceClick(resource.id)}
              >
                <a className="resource-title">{resource.title}</a>
                <p className="resource-description">{resource.description}</p>
                <div className="resource-meta">
                  <span className="meta-item">
                    <span className={`pan-badge pan-${resource.panType}`}>
                      {resource.panTypeName}
                    </span>
                  </span>
                  {resource.extractCode && (
                    <span className="meta-item">提取码: {resource.extractCode}</span>
                  )}
                  {resource.resourceTypeName && (
                    <span className="meta-item">类型: {resource.resourceTypeName}</span>
                  )}
                  {resource.sourceSite && (
                    <span className="meta-item">来源: {resource.sourceSite}</span>
                  )}
                  <span className="meta-item">点击: {resource.clickCount}</span>
                </div>
              </div>
            ))}

            {totalPages > 1 && (
              <div className="pagination">
                <button
                  className="page-button"
                  disabled={page === 0}
                  onClick={() => handlePageChange(page - 1)}
                >
                  上一页
                </button>
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  const pageNum = page < 3 ? i : page - 2 + i;
                  if (pageNum >= totalPages) return null;
                  return (
                    <button
                      key={pageNum}
                      className={`page-button ${pageNum === page ? 'active' : ''}`}
                      onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum + 1}
                    </button>
                  );
                })}
                <button
                  className="page-button"
                  disabled={page >= totalPages - 1}
                  onClick={() => handlePageChange(page + 1)}
                >
                  下一页
                </button>
              </div>
            )}
          </>
        )}
      </div>
    );
  };

  return (
    <div className="search-container">
      {!results && (
        <>
          <h2 className="search-title">全网盘搜索引擎</h2>
          <p style={{ color: '#666', marginBottom: '30px' }}>
            搜索百度网盘、阿里云盘、夸克网盘分享资源
          </p>
        </>
      )}

      <form onSubmit={handleSearch} className="search-box">
        <input
          type="text"
          className="search-input"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="输入关键词搜索网盘资源..."
        />
        <button type="submit" className="search-button" disabled={loading}>
          {loading ? '搜索中...' : '搜索'}
        </button>
      </form>

      {error && <div className="error-message">{error}</div>}

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>正在搜索中...</p>
        </div>
      )}

      {!loading && !results && hotKeywords.length > 0 && (
        <div className="hot-keywords">
          <h3>热门搜索</h3>
          <div className="keyword-tags">
            {hotKeywords.map((kw, index) => (
              <button
                key={index}
                className="keyword-tag"
                onClick={() => handleKeywordClick(kw)}
              >
                {kw}
              </button>
            ))}
          </div>
        </div>
      )}

      {!loading && renderSearchResults()}
    </div>
  );
}

export default SearchPage;
