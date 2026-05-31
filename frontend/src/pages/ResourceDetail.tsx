import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getResource } from '../services/api';
import type { Resource } from '../services/api';

function ResourceDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [resource, setResource] = useState<Resource | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (id) {
      loadResource(parseInt(id));
    }
  }, [id]);

  const loadResource = async (resourceId: number) => {
    setLoading(true);
    setError(null);

    try {
      const data = await getResource(resourceId);
      setResource(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '获取资源详情失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyLink = async () => {
    if (resource?.panUrl) {
      try {
        await navigator.clipboard.writeText(resource.panUrl);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch (err) {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = resource.panUrl;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      }
    }
  };

  const handleCopyCode = async () => {
    if (resource?.extractCode) {
      try {
        await navigator.clipboard.writeText(resource.extractCode);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch (err) {
        const textArea = document.createElement('textarea');
        textArea.value = resource.extractCode;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      }
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
      <div className="resource-detail">
        <div className="error-message">{error}</div>
        <button className="back-button" onClick={() => navigate(-1)}>
          返回
        </button>
      </div>
    );
  }

  if (!resource) {
    return (
      <div className="resource-detail">
        <div className="no-results">
          <h3>资源不存在</h3>
        </div>
        <button className="back-button" onClick={() => navigate(-1)}>
          返回
        </button>
      </div>
    );
  }

  return (
    <div>
      <button className="back-button" onClick={() => navigate(-1)}>
        返回搜索结果
      </button>

      <div className="resource-detail">
        <h1 className="detail-title">{resource.title}</h1>

        {resource.description && (
          <div className="detail-section">
            <h3>资源描述</h3>
            <p>{resource.description}</p>
          </div>
        )}

        <div className="detail-section">
          <h3>网盘链接</h3>
          <div className="pan-link-box">
            <a
              href={resource.panUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="pan-link"
            >
              {resource.panUrl}
            </a>
            <button className="copy-button" onClick={handleCopyLink}>
              {copied ? '已复制' : '复制链接'}
            </button>
          </div>

          {resource.extractCode && (
            <div className="extract-code">
              提取码: <span>{resource.extractCode}</span>
              <button
                className="copy-button"
                onClick={handleCopyCode}
                style={{ marginLeft: '10px', padding: '4px 10px', fontSize: '12px' }}
              >
                复制
              </button>
            </div>
          )}
        </div>

        <div className="detail-section">
          <h3>资源信息</h3>
          <div className="info-grid">
            <div className="info-item">
              <div className="info-label">网盘类型</div>
              <div className="info-value">
                <span className={`pan-badge pan-${resource.panType}`}>
                  {resource.panTypeName}
                </span>
              </div>
            </div>
            <div className="info-item">
              <div className="info-label">资源类型</div>
              <div className="info-value">{resource.resourceTypeName || '其他'}</div>
            </div>
            {resource.fileSize && (
              <div className="info-item">
                <div className="info-label">文件大小</div>
                <div className="info-value">{resource.fileSize}</div>
              </div>
            )}
            <div className="info-item">
              <div className="info-label">点击次数</div>
              <div className="info-value">{resource.clickCount}</div>
            </div>
            {resource.sourceSite && (
              <div className="info-item">
                <div className="info-label">来源网站</div>
                <div className="info-value">{resource.sourceSite}</div>
              </div>
            )}
            {resource.createdAt && (
              <div className="info-item">
                <div className="info-label">收录时间</div>
                <div className="info-value">{new Date(resource.createdAt).toLocaleDateString()}</div>
              </div>
            )}
          </div>
        </div>

        {resource.sourceUrl && (
          <div className="detail-section">
            <h3>来源页面</h3>
            <a
              href={resource.sourceUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="pan-link"
              style={{ fontSize: '14px' }}
            >
              {resource.sourceUrl}
            </a>
          </div>
        )}
      </div>
    </div>
  );
}

export default ResourceDetail;
