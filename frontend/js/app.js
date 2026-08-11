const App = {
    services: [],
    selectedCategory: 'ALL',

    async init() {
        await this.loadServices();
        this.checkUrlParams();
    },

    async loadServices() {
        const grid = document.getElementById('services-grid');
        if (!grid) return;

        grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1;">Loading services catalog...</div>';

        try {
            this.services = await Api.request('/services');
            this.renderServices(this.services);
        } catch (error) {
            grid.innerHTML = '<div style="color: var(--accent-danger); text-align: center; grid-column: 1/-1;">Failed to connect to backend API server on http://localhost:8081</div>';
        }
    },

    renderServices(items) {
        const grid = document.getElementById('services-grid');
        if (!grid) return;

        if (!items || items.length === 0) {
            grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1;">No services found in this category.</div>';
            return;
        }

        grid.innerHTML = items.map(svc => {
            const vendorName = svc.vendor ? svc.vendor.businessName : 'Verified Vendor';
            const vendorEmail = (svc.vendor && svc.vendor.user && svc.vendor.user.email) ? svc.vendor.user.email : 'vendor@multivendor.com';

            return `
                <div class="service-card glass" onclick="App.openBookingModal(${svc.id})">
                    <div>
                        <div class="card-header" style="align-items: flex-start;">
                            <div>
                                <span class="vendor-title" style="display: block; font-weight: 700; color: white;">${vendorName}</span>
                                <div style="margin-top: 0.3rem;">
                                    <a href="mailto:${vendorEmail}" class="email-sticker" onclick="event.stopPropagation();" title="Click to email ${vendorEmail}">${vendorEmail}</a>
                                </div>
                            </div>
                            <span class="badge badge-${svc.category.toLowerCase()}">${svc.category}</span>
                        </div>
                        <h3 class="service-title" style="margin-top: 0.6rem;">${svc.title}</h3>
                        <p class="service-desc">${svc.description || 'No description provided.'}</p>
                    </div>
                    <div class="card-footer">
                        <span style="font-size: 0.85rem; color: var(--text-secondary);">⏱ ${svc.durationMinutes} mins</span>
                        <span class="price-tag">$${svc.price.toFixed(2)}</span>
                    </div>
                </div>
            `;
        }).join('');
    },

    selectCategory(cat, btn) {
        this.selectedCategory = cat;
        document.querySelectorAll('.category-pills .pill').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        this.filterServices();
    },

    filterServices() {
        const searchVal = (document.getElementById('search-input')?.value || '').toLowerCase();
        
        const filtered = this.services.filter(svc => {
            const matchesCat = this.selectedCategory === 'ALL' || svc.category === this.selectedCategory;
            const vendorEmail = (svc.vendor && svc.vendor.user && svc.vendor.user.email) ? svc.vendor.user.email : '';
            const matchesSearch = svc.title.toLowerCase().includes(searchVal) ||
                                  (svc.description && svc.description.toLowerCase().includes(searchVal)) ||
                                  (svc.vendor && svc.vendor.businessName.toLowerCase().includes(searchVal)) ||
                                  vendorEmail.toLowerCase().includes(searchVal);
            return matchesCat && matchesSearch;
        });

        this.renderServices(filtered);
    },

    async openBookingModal(serviceId) {
        const svc = this.services.find(s => s.id === serviceId);
        if (!svc) return;

        const vendorName = svc.vendor ? svc.vendor.businessName : 'Verified Vendor';
        const vendorEmail = (svc.vendor && svc.vendor.user && svc.vendor.user.email) ? svc.vendor.user.email : 'vendor@multivendor.com';

        document.getElementById('modal-service-title').textContent = svc.title;
        document.getElementById('modal-vendor-name').innerHTML = `Provider: <strong>${vendorName}</strong> | <a href="mailto:${vendorEmail}" style="color: var(--accent-secondary); text-decoration: underline;" target="_blank">${vendorEmail}</a>`;
        document.getElementById('modal-service-price').textContent = `$${svc.price.toFixed(2)}`;

        document.getElementById('booking-modal').classList.add('active');

        // Load availability slots
        Calendar.loadSlotsForService(serviceId, svc);
    },

    closeBookingModal() {
        document.getElementById('booking-modal').classList.remove('active');
    },

    checkUrlParams() {
        const params = new URLSearchParams(window.location.search);
        const bookingRef = params.get('booking');
        const isMock = params.get('mock_payment');
        const status = params.get('status');

        if (bookingRef && (status === 'success' || isMock === 'true')) {
            // Auto confirm mock payment
            Api.request('/bookings/confirm-mock', {
                method: 'POST',
                body: JSON.stringify({ bookingReference: bookingRef })
            }).then(resp => {
                Profile.showReceiptModal(resp);
            });
        }
    }
};

document.addEventListener('DOMContentLoaded', () => App.init());
