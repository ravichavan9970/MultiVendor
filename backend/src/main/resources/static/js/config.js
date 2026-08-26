const getApiBaseUrl = () => {
    // Custom override if provided
    if (window.ENV_API_URL) {
        return window.ENV_API_URL;
    }
    // When running on Vercel or any external CDN host, target the Render backend
    if (window.location.hostname.includes('vercel.app') || window.location.hostname.includes('netlify.app')) {
        return 'https://multivendor-platform.onrender.com/api/v1';
    }
    // When running on Render or fullstack host, use same-origin relative path
    if (window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
        return '/api/v1';
    }
    // Local dev: If port is 8081 or default, use relative path; else target localhost:8081
    if (window.location.port === '8081' || !window.location.port) {
        return '/api/v1';
    }
    return 'http://localhost:8081/api/v1';
};

const CONFIG = {
    API_BASE_URL: getApiBaseUrl(),
    SWAGGER_URL: window.location.hostname.includes('vercel.app') ? 'https://multivendor-platform.onrender.com/swagger-ui.html' : '/swagger-ui.html',
    APP_NAME: 'MultiVendor Enterprise Platform',
    DEFAULT_UPI_ID: '7447661921@hdfc',
    DEFAULT_PAYEE_NAME: 'RAVINDRA LAXMAN CHAVAN'
};
