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
            const vendorName = svc.vendor ? svc.vendor.businessName : 'Verified Provider';
            const vendorEmail = (svc.vendor && svc.vendor.user && svc.vendor.user.email) ? svc.vendor.user.email : 'provider@multivendor.ai';
            const rating = (svc.vendor && svc.vendor.rating) ? svc.vendor.rating.toFixed(1) : '4.9';
            const initials = vendorName.split(' ').filter(n => n.length > 0).map(n => n[0]).join('').substring(0, 2).toUpperCase() || 'VP';

            return `
                <div class="service-card" onclick="App.openBookingModal(${svc.id})">
                    <div>
                        <div class="card-header">
                            <div class="vendor-profile-box">
                                <div class="vendor-avatar">${initials}</div>
                                <div class="vendor-info">
                                    <span class="vendor-title" title="${vendorName}">${vendorName}</span>
                                    <div class="vendor-meta-row">
                                        <a href="mailto:${vendorEmail}" class="email-sticker" onclick="event.stopPropagation();" title="Contact ${vendorEmail}">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                                            <span>${vendorEmail}</span>
                                        </a>
                                        <span class="rating-badge">★ ${rating}</span>
                                    </div>
                                </div>
                            </div>
                            <span class="badge badge-${svc.category.toLowerCase()}">${svc.category}</span>
                        </div>
                        <h3 class="service-title">${svc.title}</h3>
                        <p class="service-desc">${svc.description || 'Verified enterprise engineering and consulting session.'}</p>
                    </div>
                    <div class="card-footer">
                        <div>
                            <div style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Duration</div>
                            <span style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 600;">⏱ ${svc.durationMinutes} mins</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 0.85rem;">
                            <span class="price-tag">$${svc.price.toFixed(2)}</span>
                            <button class="btn btn-primary" style="padding: 0.4rem 0.85rem; font-size: 0.82rem;" onclick="event.stopPropagation(); App.openBookingModal(${svc.id})">Book Session →</button>
                        </div>
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
