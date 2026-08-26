const getApiBaseUrl = () => {
    if (window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
        // In Production / Cloud Deployment (Same-origin API)
        return window.ENV_API_URL || '/api/v1';
    }
    // Local dev: If port is 8081 or default, use relative path; else target localhost:8081
    if (window.location.port === '8081' || !window.location.port) {
        return '/api/v1';
    }
    return 'http://localhost:8081/api/v1';
};

const CONFIG = {
    API_BASE_URL: getApiBaseUrl(),
    SWAGGER_URL: '/swagger-ui.html',
    APP_NAME: 'MultiVendor Enterprise Platform'
};
