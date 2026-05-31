import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSystemStatus, getCrawlHistory, getAllResources } from '../services/api';
import type { SystemStatus, CrawlRecord, SearchResult } from '../services/api';

interface CrawlHistoryPage {
  content: CrawlRecord[];
  totalElements: number;
  totalPages: number;
}

function AdminPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<SystemStatus | null>(null);
  const [crawlHistory, setCrawlHistory] = useState<CrawlHistoryPage | null>(null);
  const [resources, setResources] = useState<SearchResult | null>(null);
  const [resourcePage, setResourcePage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'status' | 'history' | 'resources'>('status');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statusData, historyData] = await Promise.all([
        getSystemStatus(),
        getCrawlHistory(0, 50),
      ]);
      setStatus(statusData);
      setCrawlHistory(historyData);
    } catch (err: any) {
      setError(err.response?.data?.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const loadResources = async (page: number) => {
    try {
      const data = await getAllResources(page, 20);
      setResources(data);
      setResourcePage(page);
    } catch (err: any) {
      console.error('加载资源列表失败:', err);
    }
  };

  const handleTabChange = (tab: 'status' | 'history' | 'resources') => {
    setActiveTab(tab);
    if (tab === 'resources' && !resources) {
      loadResources(0);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
        <button className="btn btn-primary" onClick={loadData}>
          重试
        </button>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h2>系统管理</h2>
        <p className="page-desc">查看系统状态和爬取历史</p>
      </div>

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'status' ? 'active' : ''}`}
          onClick={() => handleTabChange('status')}
        >
          系统状态
        </button>
        <button
          className={`tab ${activeTab === 'resources' ? 'active' : ''}`}
          onClick={() => handleTabChange('resources')}
        >
          资源列表
        </button>
        <button
          className={`tab ${activeTab === 'history' ? 'active' : ''}`}
          onClick={() => handleTabChange('history')}
        >
          爬取历史
        </button>
      </div>

      {activeTab === 'status' && status && (
        <div className="status-panel">
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-number">{status.totalResources}</div>
              <div className="stat-label">总资源数</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">{status.validResources}</div>
              <div className="stat-label">有效资源</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">{status.todayResources}</div>
              <div className="stat-label">今日新增</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">{status.totalSearches}</div>
              <div className="stat-label">总搜索次数</div>
            </div>
          </div>

          <div className="detail-section">
            <h3>网盘分布</h3>
            <div className="pan-stats">
              <div className="pan-stat-item">
                <span className="pan-badge pan-BAIDU">百度网盘</span>
                <span className="pan-count">{status.panStats.baiduCount}</span>
              </div>
              <div className="pan-stat-item">
                <span className="pan-badge pan-ALIYUN">阿里云盘</span>
                <span className="pan-count">{status.panStats.aliyunCount}</span>
              </div>
              <div className="pan-stat-item">
                <span className="pan-badge pan-QUARK">夸克网盘</span>
                <span className="pan-count">{status.panStats.quarkCount}</span>
              </div>
              <div className="pan-stat-item">
                <span className="pan-badge pan-OTHER">其他</span>
                <span className="pan-count">{status.panStats.otherCount}</span>
              </div>
            </div>
          </div>

          <div className="detail-section">
            <h3>系统信息</h3>
            <div className="info-grid">
              <div className="info-item">
                <div className="info-label">系统状态</div>
                <div className="info-value">
                  <span className={`status-badge status-${status.status.toLowerCase()}`}>
                    {status.status}
                  </span>
                </div>
              </div>
              <div className="info-item">
                <div className="info-label">爬取任务</div>
                <div className="info-value">{status.totalCrawlTasks} 个</div>
              </div>
              <div className="info-item">
                <div className="info-label">运行中任务</div>
                <div className="info-value">{status.runningTasks} 个</div>
              </div>
              <div className="info-item">
                <div className="info-label">索引大小</div>
                <div className="info-value">{status.indexSize}</div>
              </div>
              <div className="info-item">
                <div className="info-label">运行时间</div>
                <div className="info-value">{status.uptime}</div>
              </div>
            </div>
          </div>

          <button className="btn btn-secondary" onClick={loadData}>
            刷新数据
          </button>
        </div>
      )}

      {activeTab === 'resources' && resources && (
        <div className="resources-panel">
          <div className="result-header">
            <h3>资源列表</h3>
            <div className="result-stats">
              共 {resources.totalCount} 个资源，当前第 {resourcePage + 1} 页
            </div>
          </div>

          {resources.resources.length === 0 ? (
            <div className="no-results">
              <h3>暂无资源</h3>
              <p>通过爬取功能添加资源</p>
            </div>
          ) : (
            <>
              <div className="resource-list">
                {resources.resources.map((resource) => (
                  <div
                    key={resource.id}
                    className="resource-card"
                    onClick={() => navigate(`/resource/${resource.id}`)}
                    style={{ cursor: 'pointer' }}
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
                      {resource.sourceSite && (
                        <span className="meta-item">来源: {resource.sourceSite}</span>
                      )}
                      {resource.fileSize && (
                        <span className="meta-item">大小: {resource.fileSize}</span>
                      )}
                      <span className="meta-item">
                        状态: {resource.isValid ? '有效' : '失效'}
                      </span>
                      <span className="meta-item">点击: {resource.clickCount}</span>
                    </div>
                  </div>
                ))}
              </div>

              {Math.ceil(resources.totalCount / resources.pageSize) > 1 && (
                <div className="pagination">
                  <button
                    className="page-button"
                    disabled={resourcePage === 0}
                    onClick={() => loadResources(resourcePage - 1)}
                  >
                    上一页
                  </button>
                  <span className="page-info">
                    第 {resourcePage + 1} / {Math.ceil(resources.totalCount / resources.pageSize)} 页
                  </span>
                  <button
                    className="page-button"
                    disabled={resourcePage >= Math.ceil(resources.totalCount / resources.pageSize) - 1}
                    onClick={() => loadResources(resourcePage + 1)}
                  >
                    下一页
                  </button>
                </div>
              )}
            </>
          )}

          <button className="btn btn-secondary" onClick={() => loadResources(0)}>
            刷新资源
          </button>
        </div>
      )}

      {activeTab === 'history' && crawlHistory && (
        <div className="history-panel">
          {crawlHistory.content.length === 0 ? (
            <div className="no-results">
              <h3>暂无爬取记录</h3>
              <p>使用爬取功能添加资源</p>
            </div>
          ) : (
            <div className="history-list">
              {crawlHistory.content.map((record) => (
                <div key={record.id} className="history-item">
                  <div className="history-url">{record.url}</div>
                  <div className="history-meta">
                    <span className={`status-badge status-${record.status.toLowerCase()}`}>
                      {record.status === 'COMPLETED' ? '完成' :
                       record.status === 'RUNNING' ? '运行中' :
                       record.status === 'PENDING' ? '等待中' :
                       record.status === 'FAILED' ? '失败' : record.status}
                    </span>
                    <span className="history-count">资源: {record.resourceCount}</span>
                    <span className="history-time">
                      {new Date(record.createdAt).toLocaleString()}
                    </span>
                  </div>
                  {record.errorMessage && (
                    <div className="history-error">{record.errorMessage}</div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default AdminPage;
