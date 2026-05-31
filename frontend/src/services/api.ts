import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface SearchResult {
  keyword: string;
  totalCount: number;
  page: number;
  pageSize: number;
  duration: number;
  resources: Resource[];
}

export interface Resource {
  id: number;
  title: string;
  description: string;
  panUrl: string;
  panType: string;
  panTypeName: string;
  extractCode: string;
  resourceType: string;
  resourceTypeName: string;
  sourceUrl: string;
  sourceSite: string;
  fileSize: string;
  isValid: boolean;
  clickCount: number;
  createdAt: string;
}

export interface SystemStatus {
  status: string;
  totalResources: number;
  validResources: number;
  todayResources: number;
  totalCrawlTasks: number;
  runningTasks: number;
  totalSearches: number;
  indexSize: string;
  uptime: string;
  panStats: {
    baiduCount: number;
    aliyunCount: number;
    quarkCount: number;
    otherCount: number;
  };
}

export interface CrawlRecord {
  id: number;
  url: string;
  status: string;
  resourceCount: number;
  errorMessage: string;
  startedAt: string;
  completedAt: string;
  createdAt: string;
}

// Search API
export const search = async (keyword: string, page = 0, pageSize = 20): Promise<SearchResult> => {
  const response = await api.get('/search', {
    params: { keyword, page, pageSize },
  });
  return response.data;
};

// Get resource by ID
export const getResource = async (id: number): Promise<Resource> => {
  const response = await api.get(`/resources/${id}`);
  return response.data;
};

// Get popular resources
export const getPopularResources = async (page = 0, pageSize = 10): Promise<Resource[]> => {
  const response = await api.get('/resources/popular', {
    params: { page, pageSize },
  });
  return response.data;
};

// Get latest resources
export const getLatestResources = async (page = 0, pageSize = 10): Promise<Resource[]> => {
  const response = await api.get('/resources/latest', {
    params: { page, pageSize },
  });
  return response.data;
};

// Get hot keywords
export const getHotKeywords = async (limit = 10): Promise<string[]> => {
  const response = await api.get('/keywords/hot', {
    params: { limit },
  });
  return response.data;
};

// Get system status
export const getSystemStatus = async (): Promise<SystemStatus> => {
  const response = await api.get('/status');
  return response.data;
};

// Crawl URL
export const crawlUrl = async (url: string): Promise<CrawlRecord> => {
  const response = await api.post('/crawl', { url });
  return response.data;
};

// Get crawl history
export const getCrawlHistory = async (page = 0, pageSize = 20): Promise<any> => {
  const response = await api.get('/crawl/history', {
    params: { page, pageSize },
  });
  return response.data;
};

// Get all resources with pagination
export const getAllResources = async (page = 0, pageSize = 20): Promise<SearchResult> => {
  const response = await api.get('/resources/all', {
    params: { page, pageSize },
  });
  return response.data;
};

export default api;
