const getApiBaseUrl = () => {
    if (window.location.port === '8081' || window.location.hostname === 'localhost' && !window.location.port) {
        return '/api/v1';
    }
    return 'http://localhost:8081/api/v1';
};

const CONFIG = {
    API_BASE_URL: getApiBaseUrl(),
    SWAGGER_URL: 'http://localhost:8081/swagger-ui.html',
    APP_NAME: 'MultiVendor Enterprise Platform'
};
