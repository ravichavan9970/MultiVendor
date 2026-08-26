const Vendor = {
    vendorId: null,
    services: [],
    revenueChart: null,

    async init() {
        const user = Api.getUser();
        if (!user || (user.role !== 'VENDOR' && user.role !== 'ADMIN')) {
            Auth.showLoginModal();
            const emailInput = document.getElementById('login-email');
            const passInput = document.getElementById('login-password');
            if (emailInput && passInput) {
                emailInput.value = 'vendor.alex@multivendor.com';
                passInput.value = 'password123';
            }
            const regRole = document.getElementById('reg-role');
            if (regRole) regRole.value = 'VENDOR';
            return;
        }

        // Restore cached vendor services immediately
        const cached = localStorage.getItem('mv_persistent_vendor_services') || localStorage.getItem('mv_vendor_services');
        if (cached) {
            try {
                this.services = JSON.parse(cached);
                this.filterServices();
            } catch (e) {}
        }

        await this.syncLocalBackupToServer();
        await this.loadVendorData();
        await this.loadVendorServices();
        await this.loadPendingVerifications();
        await this.loadVendorBookings();
    },

    async syncLocalBackupToServer() {
        const token = Api.getToken();
        if (!token) return;

        try {
            const serverServices = await Api.request('/services').catch(() => []);
            const localBackup = localStorage.getItem('mv_persistent_vendor_services') || localStorage.getItem('mv_vendor_services');
            
            // If server database restarted/wiped but local storage has user services:
            if ((!serverServices || serverServices.length === 0) && localBackup) {
                const items = JSON.parse(localBackup);
                if (items && items.length > 0) {
                    console.log('🔄 Auto-syncing and permanently storing local services to backend server...');
                    for (const svc of items) {
                        try {
                            const created = await Api.request('/services', {
                                method: 'POST',
                                body: JSON.stringify({
                                    title: svc.title,
                                    category: svc.category || 'TUTORING',
                                    price: svc.price,
                                    durationMinutes: svc.durationMinutes || 60,
                                    meetingLink: svc.meetingLink || 'https://meet.google.com/dae-zpiu-oau',
                                    description: svc.description || ''
                                })
                            });
                            // Auto generate 7 days of recurring slots
                            if (created && created.id) {
                                await Api.request('/slots/batch-generate', {
                                    method: 'POST',
                                    body: JSON.stringify({
                                        serviceId: created.id,
                                        daysAhead: 7,
                                        startHour: 9,
                                        endHour: 17,
                                        slotDurationMinutes: 60
                                    })
                                }).catch(() => {});
                            }
                        } catch (err) {}
                    }
                }
            }
        } catch (e) {}
    },

    async loadVendorData() {
        try {
            const profile = await Api.request('/vendors/my-profile');
            this.vendorId = profile.id;

            document.getElementById('vendor-business-name').textContent = profile.businessName;
            document.getElementById('vendor-details').textContent = `Category: ${profile.category} | Hourly Rate: $${profile.hourlyRate} | Rating: ${profile.rating} ★`;

            // Load revenue stats
            const stats = await Api.request(`/vendors/${this.vendorId}/stats`);
            document.getElementById('stat-revenue').textContent = `$${stats.totalRevenue.toFixed(2)}`;
            document.getElementById('stat-confirmed').textContent = stats.totalConfirmedBookings;
            document.getElementById('stat-pending').textContent = stats.totalPendingBookings;
            document.getElementById('stat-rating').textContent = `${stats.averageRating.toFixed(2)} ★`;

            this.renderChart(stats);
        } catch (error) {
            console.error(error);
        }
    },

    async loadVendorServices() {
        const select = document.getElementById('gen-service-id');

        try {
            const fresh = await Api.request('/services');
            if (fresh && fresh.length > 0) {
                this.services = fresh;
                localStorage.setItem('mv_vendor_services', JSON.stringify(fresh));
                localStorage.setItem('mv_cached_services', JSON.stringify(fresh));
                localStorage.setItem('mv_persistent_vendor_services', JSON.stringify(fresh));
            } else if (fresh && fresh.length === 0) {
                const persistent = localStorage.getItem('mv_persistent_vendor_services');
                if (persistent) {
                    try {
                        const parsed = JSON.parse(persistent);
                        if (parsed && parsed.length > 0) {
                            this.services = parsed;
                        }
                    } catch (e) {}
                }
            }
            
            // Populate Batch Slot Generator Dropdown
            if (select) {
                select.innerHTML = this.services.map(s => `<option value="${s.id}">${s.title} ($${s.price.toFixed(2)})</option>`).join('');
            }

            const searchInput = document.getElementById('vendor-service-search');
            const searchTerm = searchInput ? searchInput.value : '';
            this.filterServices(searchTerm);
        } catch (error) {
            if (!this.services || this.services.length === 0) {
                const tbody = document.getElementById('vendor-services-body');
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--accent-danger);">Failed to load services.</td></tr>';
            }
        }
    },

    filterServices(query = '') {
        const tbody = document.getElementById('vendor-services-body');
        if (!tbody) return;

        const term = query.trim().toLowerCase();
        const filtered = this.services.filter(s => {
            const titleMatch = s.title.toLowerCase().includes(term);
            const categoryMatch = s.category.toLowerCase().includes(term);
            const descMatch = (s.description || '').toLowerCase().includes(term);
            return titleMatch || categoryMatch || descMatch;
        });

        if (!filtered || filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">${term ? `No service offerings match "${query}".` : 'No service offerings created yet. Click "+ Add New Service" above!'}</td></tr>`;
            return;
        }

        tbody.innerHTML = filtered.map(s => `
            <tr style="border-bottom: 1px solid var(--border-glass);">
                <td style="padding: 0.8rem;">
                    <div style="font-weight: 700; color: white;">${s.title}</div>
                    <div style="font-size: 0.75rem; color: var(--text-secondary); max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${s.description || 'No description provided'}</div>
                </td>
                <td style="padding: 0.8rem;"><span class="badge badge-tutoring" style="font-size:0.75rem;">${s.category}</span></td>
                <td style="padding: 0.8rem; font-weight: 800; color: var(--accent-success);">$${s.price.toFixed(2)}</td>
                <td style="padding: 0.8rem; font-size: 0.85rem;">⏱ ${s.durationMinutes} mins</td>
                <td style="padding: 0.8rem;"><span style="color: var(--accent-success); font-size: 0.8rem;">● Active</span></td>
                <td style="padding: 0.8rem; text-align: right;">
                    <div style="display: flex; gap: 0.4rem; justify-content: flex-end; flex-wrap: wrap;">
                        <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="Vendor.showAddSlotModal(${s.id}, '${s.title.replace(/'/g, "\\'")}')">➕ Add Slot</button>
                        <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="Vendor.showEditServiceModal(${s.id})">✏️ Edit</button>
                        <button class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="Vendor.deleteService(${s.id})">🗑️</button>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    async loadVendorBookings() {
        const tbody = document.getElementById('vendor-bookings-body');
        if (!tbody) return;

        try {
            if (!this.vendorId) {
                const profile = await Api.request('/vendors/my-profile');
                this.vendorId = profile.id;
            }
            const bookings = await Api.request(`/bookings/vendor/${this.vendorId}`);
            this.loadedBookings = bookings || [];
            
            if (!bookings || bookings.length === 0) {
                tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">No incoming customer bookings recorded yet.</td></tr>';
                return;
            }

            tbody.innerHTML = bookings.map(b => {
                const customerEmail = b.customerEmail || 'No email';
                const customerPhone = b.customerPhone || 'Not provided';
                const notesSnippet = b.customerNotes ? (b.customerNotes.length > 25 ? b.customerNotes.substring(0, 25) + '...' : b.customerNotes) : 'No notes';

                return `
                    <tr style="border-bottom: 1px solid var(--border-glass);">
                        <td style="padding: 0.8rem; font-family: monospace; font-size: 0.8rem;">${b.bookingReference.substring(0, 8)}...</td>
                        <td style="padding: 0.8rem; font-weight: 600; color: white;">${b.customerName}</td>
                        <td style="padding: 0.8rem; font-size: 0.85rem;">
                            <a href="mailto:${customerEmail}" class="email-sticker" onclick="event.stopPropagation();" title="Email customer">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                                <span class="email-text">${customerEmail}</span>
                            </a>
                            <div style="font-size: 0.75rem; color: var(--accent-secondary); margin-top: 0.3rem;">📞 ${customerPhone}</div>
                        </td>
                        <td style="padding: 0.8rem;">${b.serviceTitle}</td>
                        <td style="padding: 0.8rem; font-size: 0.85rem; color: var(--accent-secondary);">${new Date(b.startTime).toLocaleString()}</td>
                        <td style="padding: 0.8rem; font-weight: 700; color: var(--accent-success);">$${b.totalAmount.toFixed(2)}</td>
                        <td style="padding: 0.8rem;"><span class="badge badge-${b.status.toLowerCase()}">${b.status}</span></td>
                        <td style="padding: 0.8rem; font-size: 0.8rem; color: var(--text-secondary); max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${notesSnippet}</td>
                        <td style="padding: 0.8rem; text-align: right;">
                            <div style="display: flex; gap: 0.4rem; justify-content: flex-end; align-items: center;">
                                <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="Vendor.showCustomerDetails('${b.bookingReference}')">👁️ View Details</button>
                                ${b.status === 'HOLD_PENDING_PAYMENT' ? 
                                  `<button class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="Vendor.confirmMockBooking('${b.bookingReference}')">Simulate Payment</button>` : 
                                  `<span style="color: var(--accent-success); font-size: 0.8rem;">Confirmed</span>`}
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');

        } catch (error) {
            console.error('Vendor Bookings:', error);
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">No incoming customer bookings recorded yet.</td></tr>';
        }
    },

    async loadPendingVerifications() {
        const tbody = document.getElementById('utr-verification-body');
        const badge = document.getElementById('pending-utr-count');
        if (!tbody) return;

        try {
            const list = await Api.request('/bookings/pending-verifications');
            if (badge) badge.textContent = `${list ? list.length : 0} Pending`;

            if (!list || list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">🎉 All UPI & UTR payments verified! No pending requests.</td></tr>';
                return;
            }

            tbody.innerHTML = list.map(b => {
                const utr = b.utrNumber || 'N/A';
                return `
                    <tr style="border-bottom: 1px solid var(--border-glass);">
                        <td style="padding: 0.8rem;">
                            <div style="font-weight: 700; color: white;">${b.customerName}</div>
                            <div style="font-size: 0.75rem; color: var(--text-secondary);">${b.customerEmail} | ${b.customerPhone || ''}</div>
                        </td>
                        <td style="padding: 0.8rem;">
                            <div style="font-weight: 600;">${b.serviceTitle}</div>
                            <div style="font-weight: 800; color: var(--accent-success); font-size: 0.95rem;">$${b.totalAmount.toFixed(2)}</div>
                        </td>
                        <td style="padding: 0.8rem;">
                            <div style="display: flex; align-items: center; gap: 0.4rem;">
                                <span class="badge" style="background: rgba(99, 102, 241, 0.15); color: #818cf8; font-family: monospace; font-size: 0.88rem; font-weight: 700; padding: 0.3rem 0.6rem; border: 1px solid rgba(99, 102, 241, 0.4);">${utr}</span>
                                <button class="btn btn-secondary" style="padding: 0.2rem 0.45rem; font-size: 0.72rem;" onclick="navigator.clipboard.writeText('${utr}'); Api.showToast('Copied UTR: ${utr}', 'info');">📋</button>
                            </div>
                            <div style="font-size: 0.72rem; color: var(--text-secondary); margin-top: 0.2rem;">Ref: ${b.bookingReference.substring(0, 8)}...</div>
                        </td>
                        <td style="padding: 0.8rem; font-size: 0.82rem; color: var(--accent-secondary);">
                            ${new Date(b.startTime).toLocaleString()}
                        </td>
                        <td style="padding: 0.8rem;">
                            <span class="badge badge-amber" style="font-size: 0.75rem;">⏳ Needs Verification</span>
                        </td>
                        <td style="padding: 0.8rem; text-align: right;">
                            <div style="display: flex; gap: 0.4rem; justify-content: flex-end;">
                                <button class="btn btn-primary" style="background: var(--brand-emerald); border-color: var(--brand-emerald); padding: 0.35rem 0.75rem; font-size: 0.78rem;" onclick="Vendor.verifyUtr('${b.bookingReference}', true)">✅ Approve & Confirm</button>
                                <button class="btn btn-danger" style="padding: 0.35rem 0.65rem; font-size: 0.78rem;" onclick="Vendor.verifyUtr('${b.bookingReference}', false)">❌ Reject</button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">No pending UTR verifications.</td></tr>';
        }
    },

    async verifyUtr(bookingRef, approve) {
        let reason = null;
        if (!approve) {
            reason = prompt('Please enter a reason for rejecting this UTR (or leave empty):', 'Invalid or unreceived transaction number');
            if (reason === null) return;
        }

        try {
            await Api.request('/bookings/verify-utr', {
                method: 'POST',
                body: JSON.stringify({
                    bookingReference: bookingRef,
                    approve: approve,
                    rejectionReason: reason
                })
            });

            Api.showToast(approve ? '✅ UTR Approved! Booking confirmed and slot locked.' : '❌ UTR Rejected. Slot released back to public marketplace.', approve ? 'success' : 'info');
            await this.loadPendingVerifications();
            await this.loadVendorBookings();
            await this.loadVendorData();
        } catch (e) {
            // Handled
        }
    },

    renderChart(stats) {
        const ctx = document.getElementById('revenueChart');
        if (!ctx) return;

        if (this.revenueChart) this.revenueChart.destroy();

        this.revenueChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Confirmed Revenue', 'Pending Holds', 'Completed Services'],
                datasets: [{
                    label: 'Vendor Metrics ($)',
                    data: [stats.totalRevenue, stats.totalPendingBookings * 75, stats.totalConfirmedBookings * 85],
                    backgroundColor: ['rgba(16, 185, 129, 0.6)', 'rgba(245, 158, 11, 0.6)', 'rgba(99, 102, 241, 0.6)'],
                    borderColor: ['#10b981', '#f59e0b', '#6366f1'],
                    borderWidth: 2,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#9ca3af', font: { size: 10 } } },
                    x: { grid: { display: false }, ticks: { color: '#9ca3af', font: { size: 10 } } }
                }
            }
        });
    },

    showCreateServiceModal() {
        document.getElementById('create-service-modal').classList.add('active');
    },

    closeCreateServiceModal() {
        document.getElementById('create-service-modal').classList.remove('active');
    },

    async handleCreateService(event) {
        event.preventDefault();
        const title = document.getElementById('svc-title').value;
        const category = document.getElementById('svc-category').value;
        const price = parseFloat(document.getElementById('svc-price').value);
        const durationMinutes = parseInt(document.getElementById('svc-duration').value);
        const meetingLink = document.getElementById('svc-meeting-link')?.value || 'https://meet.google.com/dae-zpiu-oau';
        const description = document.getElementById('svc-desc').value;

        try {
            await Api.request('/services', {
                method: 'POST',
                body: JSON.stringify({ title, category, price, durationMinutes, meetingLink, description })
            });

            Api.showToast('Service created & published successfully!', 'success');
            this.closeCreateServiceModal();
            await this.loadVendorServices();
            await this.loadVendorData();
        } catch (error) {
            // Handled
        }
    },

    async showEditServiceModal(serviceId) {
        try {
            const service = await Api.request(`/services/${serviceId}`);
            document.getElementById('edit-svc-id').value = service.id;
            document.getElementById('edit-svc-title').value = service.title;
            document.getElementById('edit-svc-category').value = service.category;
            document.getElementById('edit-svc-price').value = service.price;
            document.getElementById('edit-svc-duration').value = service.durationMinutes;
            document.getElementById('edit-svc-meeting-link').value = service.meetingLink || 'https://meet.google.com/dae-zpiu-oau';
            document.getElementById('edit-svc-desc').value = service.description || '';

            document.getElementById('edit-service-modal').classList.add('active');
        } catch (error) {
            // Handled
        }
    },

    closeEditServiceModal() {
        document.getElementById('edit-service-modal').classList.remove('active');
    },

    async handleUpdateService(event) {
        event.preventDefault();
        const serviceId = document.getElementById('edit-svc-id').value;
        const title = document.getElementById('edit-svc-title').value;
        const category = document.getElementById('edit-svc-category').value;
        const price = parseFloat(document.getElementById('edit-svc-price').value);
        const durationMinutes = parseInt(document.getElementById('edit-svc-duration').value);
        const meetingLink = document.getElementById('edit-svc-meeting-link')?.value || 'https://meet.google.com/dae-zpiu-oau';
        const description = document.getElementById('edit-svc-desc').value;

        try {
            await Api.request(`/services/${serviceId}`, {
                method: 'PUT',
                body: JSON.stringify({ title, category, price, durationMinutes, meetingLink, description })
            });

            Api.showToast('Service details updated successfully!', 'success');
            this.closeEditServiceModal();
            await this.loadVendorServices();
        } catch (error) {
            // Handled
        }
    },

    async deleteService(serviceId) {
        if (!confirm('Are you sure you want to delete/deactivate this service offering?')) return;

        try {
            await Api.request(`/services/${serviceId}`, {
                method: 'DELETE'
            });

            Api.showToast('Service deactivated successfully', 'info');
            await this.loadVendorServices();
        } catch (error) {
            // Handled
        }
    },

    async deleteAllServices() {
        if (!confirm('🚨 Are you sure you want to DELETE ALL service offerings? This action cannot be undone!')) return;

        try {
            await Api.request('/services/all', {
                method: 'DELETE'
            });

            Api.showToast('All service offerings have been deactivated', 'info');
            await this.loadVendorServices();
        } catch (error) {
            // Handled
        }
    },

    showAddSlotModal(serviceId, title) {
        document.getElementById('slot-svc-id').value = serviceId;
        document.getElementById('slot-svc-title').value = title;

        // Default date to tomorrow
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('slot-date').value = tomorrow.toISOString().split('T')[0];

        document.getElementById('add-slot-modal').classList.add('active');
    },

    closeAddSlotModal() {
        document.getElementById('add-slot-modal').classList.remove('active');
    },

    async handleCreateSingleSlot(event) {
        event.preventDefault();
        const serviceId = parseInt(document.getElementById('slot-svc-id').value);
        const dateStr = document.getElementById('slot-date').value;
        const startTimeStr = document.getElementById('slot-start-time').value;
        const endTimeStr = document.getElementById('slot-end-time').value;

        const startTime = `${dateStr}T${startTimeStr}:00`;
        const endTime = `${dateStr}T${endTimeStr}:00`;

        try {
            await Api.request('/services/slot', {
                method: 'POST',
                body: JSON.stringify({ serviceId, startTime, endTime })
            });

            Api.showToast('➕ Custom availability slot created successfully!', 'success');
            this.closeAddSlotModal();
        } catch (error) {
            // Handled
        }
    },

    async handleBatchGenerate(event) {
        event.preventDefault();
        const serviceId = parseInt(document.getElementById('gen-service-id').value);
        const daysCount = parseInt(document.getElementById('gen-days').value);
        const startHour = parseInt(document.getElementById('gen-start-hour').value);
        const endHour = parseInt(document.getElementById('gen-end-hour').value);

        const startDate = new Date();
        startDate.setDate(startDate.getDate() + 1);

        try {
            const slots = await Api.request('/services/batch-slots', {
                method: 'POST',
                body: JSON.stringify({
                    serviceId,
                    startDate: startDate.toISOString(),
                    daysCount,
                    startHour,
                    endHour,
                    slotDurationMinutes: 60
                })
            });

            Api.showToast(`✨ Generated ${slots.length} availability slots!`, 'success');
        } catch (error) {
            // Handled
        }
    },

    async confirmMockBooking(ref) {
        try {
            await Api.request('/bookings/confirm-mock', {
                method: 'POST',
                body: JSON.stringify({ bookingReference: ref })
            });

            Api.showToast('Simulated Stripe payment succeeded!', 'success');
            await this.loadVendorBookings();
            await this.loadVendorData();
        } catch (error) {
            // Handled
        }
    },

    async resetSystem() {
        if (!confirm('⚠️ Are you sure you want to RESET all transaction history, earnings, confirmed bookings, and custom users? This action cannot be undone.')) {
            return;
        }

        try {
            await Api.request('/admin/reset-system', { method: 'POST' });
            Api.showToast('🧹 System reset complete! Wiped history, earnings & custom accounts.', 'success');
            setTimeout(() => window.location.reload(), 800);
        } catch (error) {
            Api.showToast(error.message || 'Failed to reset system', 'error');
        }
    },

    showCustomerDetails(bookingRef) {
        const booking = (this.loadedBookings || []).find(b => b.bookingReference === bookingRef);
        if (!booking) return;

        const customerEmail = booking.customerEmail || '';
        const customerPhone = booking.customerPhone || 'Not provided';
        const meetingLink = booking.meetingLink || 'https://meet.google.com/dae-zpiu-oau';
        const formattedTime = new Date(booking.startTime).toLocaleString();
        const vendorName = booking.vendorBusinessName || 'Provider';

        const subject = `Confirmed Appointment & Video Meeting Link: ${booking.serviceTitle}`;
        const body = `Hi ${booking.customerName || 'Valued Customer'},\n\nThank you for booking your appointment with ${vendorName}!\n\nAppointment Details:\n-----------------------------------\n📌 Service: ${booking.serviceTitle}\n📅 Date & Time: ${formattedTime}\n💰 Total Amount: $${booking.totalAmount.toFixed(2)}\n🔖 Booking Ref: #${booking.bookingReference}\n\n🎥 Join Video Meeting Room Link:\n${meetingLink}\n\n💬 Customer Questions / Special Notes:\n"${booking.customerNotes || 'N/A'}"\n\nPlease feel free to reply directly to this email if you have any questions before our session.\n\nBest regards,\n${vendorName}`;

        const mailtoUrl = `mailto:${customerEmail}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;

        document.getElementById('detail-customer-name').textContent = booking.customerName || 'Customer';
        document.getElementById('detail-customer-email').innerHTML = `<a href="${mailtoUrl}" class="email-sticker" title="Click to open mail app & compose pre-filled email"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg><span class="email-text">${customerEmail}</span></a>`;
        document.getElementById('detail-customer-phone').innerHTML = `<a href="tel:${customerPhone}" style="color: var(--accent-secondary); text-decoration: underline;">${customerPhone}</a>`;
        document.getElementById('detail-service-title').textContent = booking.serviceTitle;
        document.getElementById('detail-slot-time').textContent = formattedTime;
        document.getElementById('detail-amount').textContent = `$${booking.totalAmount.toFixed(2)}`;
        document.getElementById('detail-ref-code').textContent = `(#${booking.bookingReference.substring(0, 12)}...)`;
        document.getElementById('detail-customer-notes').textContent = booking.customerNotes || 'No special questions or notes submitted by customer.';

        // Display Meeting Link in Modal
        const meetingLinkEl = document.getElementById('detail-meeting-link');
        if (meetingLinkEl) {
            meetingLinkEl.href = meetingLink;
            meetingLinkEl.textContent = meetingLink;
        }

        document.getElementById('detail-email-btn').href = mailtoUrl;
        document.getElementById('detail-call-btn').href = `tel:${customerPhone}`;

        document.getElementById('customer-details-modal').classList.add('active');
    },

    closeCustomerDetailsModal() {
        document.getElementById('customer-details-modal').classList.remove('active');
    }
};

document.addEventListener('DOMContentLoaded', () => Vendor.init());
