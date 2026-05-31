import { useState } from 'react';
import { crawlUrl } from '../services/api';
import type { CrawlRecord } from '../services/api';

function CrawlPage() {
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<CrawlRecord | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleCrawl = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!url.trim()) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await crawlUrl(url.trim());
      setResult(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '爬取失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const quickCrawlOptions = [
    { label: '百度网盘热搜', url: 'https://pan.baidu.com' },
    { label: '阿里云盘', url: 'https://www.alipan.com' },
    { label: '夸克网盘', url: 'https://pan.quark.cn' },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <h2>资源爬取</h2>
        <p className="page-desc">输入网址，自动解析页面中的网盘资源链接</p>
      </div>

      <form onSubmit={handleCrawl} className="crawl-form">
        <div className="input-group">
          <input
            type="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="请输入包含网盘资源的网页URL..."
            className="crawl-input"
            disabled={loading}
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? '爬取中...' : '开始爬取'}
          </button>
        </div>
      </form>

      <div className="quick-options">
        <h3>快速爬取</h3>
        <div className="options-grid">
          {quickCrawlOptions.map((opt, index) => (
            <button
              key={index}
              className="option-card"
              onClick={() => {
                setUrl(opt.url);
              }}
            >
              <span className="option-label">{opt.label}</span>
              <span className="option-url">{opt.url}</span>
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>正在爬取页面，解析网盘链接...</p>
        </div>
      )}

      {error && <div className="error-message">{error}</div>}

      {result && (
        <div className="crawl-result">
          <h3>爬取结果</h3>
          <div className="result-info">
            <div className="info-row">
              <span className="info-label">爬取地址:</span>
              <span className="info-value">{result.url}</span>
            </div>
            <div className="info-row">
              <span className="info-label">状态:</span>
              <span className={`status-badge status-${result.status.toLowerCase()}`}>
                {result.status === 'COMPLETED' ? '完成' : result.status}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">发现资源:</span>
              <span className="info-value">{result.resourceCount} 个</span>
            </div>
            {result.errorMessage && (
              <div className="info-row">
                <span className="info-label">错误信息:</span>
                <span className="info-value error">{result.errorMessage}</span>
              </div>
            )}
          </div>
          {result.resourceCount > 0 && (
            <div className="result-actions">
              <a href={`/?q=${encodeURIComponent(new URL(url).hostname)}`} className="btn btn-link">
                查看爬取的资源
              </a>
            </div>
          )}
        </div>
      )}

      <div className="crawl-tips">
        <h3>使用说明</h3>
        <ul>
          <li>输入包含网盘分享链接的网页URL</li>
          <li>系统会自动识别百度网盘、阿里云盘、夸克网盘等链接</li>
          <li>支持自动提取分享链接和提取码</li>
          <li>爬取的资源会自动添加到搜索引擎索引中</li>
        </ul>
      </div>
    </div>
  );
}

export default CrawlPage;
