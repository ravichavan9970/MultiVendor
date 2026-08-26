const Api = {
    getToken() {
        return localStorage.getItem('mv_token');
    },

    setToken(token) {
        localStorage.setItem('mv_token', token);
    },

    getUser() {
        const user = localStorage.getItem('mv_user');
        return user ? JSON.parse(user) : null;
    },

    setUser(user) {
        localStorage.setItem('mv_user', JSON.stringify(user));
    },

    clearAuth() {
        localStorage.removeItem('mv_token');
        localStorage.removeItem('mv_user');
    },

    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                if (response.status === 401 && this.getToken() && (endpoint.includes('/users/me') || endpoint.includes('/vendors/my-profile'))) {
                    const errorMessage = "Session expired. Please log in again.";
                    this.showToast(errorMessage, 'info');
                    this.clearAuth();
                    throw new Error(errorMessage);
                }
                const errorMessage = data.message || data.error || `HTTP Error ${response.status}`;
                if (!endpoint.includes('/stats') && !endpoint.includes('/services') && !endpoint.includes('/auth/login')) {
                    this.showToast(errorMessage, 'danger');
                }
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`API Error on ${endpoint}:`, error);
            throw error;
        }
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container') || this.createToastContainer();
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()">✕</button>
        `;
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 4000);
    },

    createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
        return container;
    }
};
