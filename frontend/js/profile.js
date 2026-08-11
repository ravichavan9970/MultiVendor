const Profile = {
    async showProfileModal() {
        if (!Api.getToken()) {
            Api.showToast('Please log in first', 'info');
            Auth.showLoginModal();
            return;
        }

        const modal = document.getElementById('profile-modal');
        if (modal) modal.classList.add('active');

        await this.loadProfileData();
    },

    closeProfileModal() {
        const modal = document.getElementById('profile-modal');
        if (modal) modal.classList.remove('active');
    },

    switchTab(tabName, btn) {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

        btn.classList.add('active');
        document.getElementById(`tab-${tabName}`).classList.add('active');
    },

    async loadProfileData() {
        try {
            const data = await Api.request('/users/me');
            
            document.getElementById('prof-email').value = data.email || '';
            document.getElementById('prof-name').value = data.fullName || '';
            document.getElementById('prof-role').value = data.role || 'CUSTOMER';

            const fullPhone = (data.phoneNumber || '').trim();
            const ccSelect = document.getElementById('prof-country-code');
            const phoneInput = document.getElementById('prof-phone');
            
            if (ccSelect && phoneInput) {
                const knownCodes = ['+880', '+971', '+91', '+44', '+61', '+49', '+33', '+81', '+55', '+86', '+65', '+92', '+27', '+34', '+39', '+1'];
                
                let matchedCode = null;
                let cleanNumber = fullPhone;

                for (const code of knownCodes) {
                    if (fullPhone.startsWith(code)) {
                        matchedCode = code;
                        cleanNumber = fullPhone.substring(code.length).replace(/^[\s\-]+/, '');
                        break;
                    }
                }

                if (matchedCode) {
                    ccSelect.value = matchedCode;
                    phoneInput.value = cleanNumber;
                } else {
                    phoneInput.value = fullPhone;
                }
            }

            const vendorFields = document.getElementById('prof-vendor-fields');
            if (data.role === 'VENDOR' || data.role === 'ADMIN') {
                if (vendorFields) vendorFields.style.display = 'block';
                document.getElementById('prof-business-name').value = data.businessName || '';
                document.getElementById('prof-category').value = data.category || 'TUTORING';
                document.getElementById('prof-bio').value = data.bio || '';
                document.getElementById('prof-location').value = data.location || '';
                document.getElementById('prof-rate').value = data.hourlyRate || 50;
            } else {
                if (vendorFields) vendorFields.style.display = 'none';
            }
        } catch (error) {
            console.error('Profile load error:', error);
        }
    },

    async handleUpdateProfile(event) {
        event.preventDefault();
        const fullName = document.getElementById('prof-name').value;
        const cc = document.getElementById('prof-country-code')?.value || '+91';
        let rawNum = (document.getElementById('prof-phone')?.value || '').trim();
        
        rawNum = rawNum.replace(/^\+\d+[\s\-]?/, '').replace(/^[\s\-]+/, '');
        const phoneNumber = rawNum ? `${cc} ${rawNum}` : '';
        const role = document.getElementById('prof-role').value;

        const body = { fullName, phoneNumber };

        if (role === 'VENDOR' || role === 'ADMIN') {
            body.businessName = document.getElementById('prof-business-name').value;
            body.category = document.getElementById('prof-category').value;
            body.bio = document.getElementById('prof-bio').value;
            body.location = document.getElementById('prof-location').value;
            body.hourlyRate = parseFloat(document.getElementById('prof-rate').value || 50);
        }

        try {
            const updated = await Api.request('/users/profile', {
                method: 'PUT',
                body: JSON.stringify(body)
            });

            const currentUser = Api.getUser() || {};
            currentUser.fullName = updated.fullName;
            Api.setUser(currentUser);

            Auth.updateNavUI();
            Api.showToast('Profile updated successfully! ✨', 'success');
            this.closeProfileModal();
        } catch (error) {
            // Handled
        }
    },

    async handleChangePassword(event) {
        event.preventDefault();
        const currentPassword = document.getElementById('pass-current').value;
        const newPassword = document.getElementById('pass-new').value;
        const confirmPassword = document.getElementById('pass-confirm').value;

        if (newPassword !== confirmPassword) {
            Api.showToast('New password and confirm password do not match!', 'danger');
            return;
        }

        try {
            await Api.request('/users/change-password', {
                method: 'PUT',
                body: JSON.stringify({ currentPassword, newPassword })
            });

            Api.showToast('🔒 Password changed successfully!', 'success');
            document.getElementById('pass-current').value = '';
            document.getElementById('pass-new').value = '';
            document.getElementById('pass-confirm').value = '';
            this.closeProfileModal();
        } catch (error) {
            // Handled
        }
    },

    async deleteAccount() {
        if (!confirm('⚠️ Are you sure you want to permanently delete your account? This action cannot be undone.')) {
            return;
        }

        try {
            await Api.request('/users/me', {
                method: 'DELETE'
            });

            Api.showToast('Account deleted successfully', 'info');
            this.closeProfileModal();
            Auth.logout();
        } catch (error) {
            // Handled
        }
    },

    async showMyBookingsModal() {
        if (!Api.getToken()) {
            Api.showToast('Please log in first', 'info');
            Auth.showLoginModal();
            return;
        }

        const modal = document.getElementById('my-bookings-modal');
        if (modal) modal.classList.add('active');

        await this.loadMyBookings();
    },

    closeMyBookingsModal() {
        const modal = document.getElementById('my-bookings-modal');
        if (modal) modal.classList.remove('active');
    },

    async loadMyBookings() {
        const list = document.getElementById('customer-bookings-list');
        if (!list) return;

        list.innerHTML = '<div style="text-align:center; color:var(--text-secondary); padding:2rem;">Loading your appointments...</div>';

        try {
            const bookings = await Api.request('/bookings/my-bookings');
            this.myBookings = bookings || [];

            if (!bookings || bookings.length === 0) {
                list.innerHTML = '<div style="text-align:center; color:var(--text-secondary); padding:2rem;">You have not made any bookings yet. Browse the marketplace above!</div>';
                return;
            }

            const now = new Date();

            list.innerHTML = bookings.map(b => {
                const createdTime = b.holdExpiresAt ? new Date(new Date(b.holdExpiresAt).getTime() - 10 * 60 * 1000) : now;
                const hoursPassed = (now - createdTime) / (1000 * 60 * 60);
                const isCancellable = b.status !== 'CANCELLED' && hoursPassed <= 1.0;

                const meetingLink = b.meetingLink || 'https://meet.google.com/dae-zpiu-oau';

                return `
                <div class="glass" style="padding: 1.2rem; margin-bottom: 1rem; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
                    <div>
                        <div style="display:flex; gap:0.5rem; align-items:center; margin-bottom:0.3rem;">
                            <span style="font-weight:700; font-size:1.1rem; color:white;">${b.serviceTitle}</span>
                            <span class="badge badge-${b.status.toLowerCase()}">${b.status}</span>
                        </div>
                        <div style="font-size:0.85rem; color:var(--accent-secondary);">Provider: ${b.vendorBusinessName}</div>
                        <div style="font-size:0.8rem; color:var(--text-secondary); margin-top:0.3rem;">
                            📅 ${new Date(b.startTime).toLocaleString()} | Ref: <code style="color:#a5b4fc;">${b.bookingReference.substring(0,8)}...</code>
                        </div>
                    </div>
                    <div style="text-align:right; display:flex; flex-direction:column; align-items:flex-end; gap:0.4rem;">
                        <div style="font-size:1.3rem; font-weight:800; color:var(--accent-success);">$${b.totalAmount.toFixed(2)}</div>
                        <div style="display:flex; gap:0.5rem; align-items:center; flex-wrap:wrap;">
                            <a href="${meetingLink}" target="_blank" class="btn btn-secondary" style="font-size:0.75rem; padding:0.35rem 0.7rem; text-decoration:none; color:#38bdf8; border-color:rgba(56, 189, 248, 0.4);">🎥 Join Video Meeting</a>
                            <button class="btn btn-secondary" style="font-size:0.75rem; padding:0.35rem 0.7rem;" onclick="Profile.showReceiptByRef('${b.bookingReference}')">Receipt</button>
                            ${isCancellable ? `
                                <button class="btn btn-danger" style="font-size:0.75rem; padding:0.35rem 0.7rem;" onclick="Profile.cancelBooking(${b.id})">Cancel Appointment</button>
                            ` : (b.status !== 'CANCELLED' ? `<span style="font-size:0.75rem; color:var(--text-secondary); opacity:0.7;">Non-Cancellable (>1h)</span>` : '')}
                        </div>
                    </div>
                </div>
            `;}).join('');

        } catch (error) {
            list.innerHTML = '<div style="text-align:center; color:var(--accent-danger); padding:2rem;">Failed to load bookings.</div>';
        }
    },

    showReceiptByRef(bookingRef) {
        const booking = (this.myBookings || []).find(b => b.bookingReference === bookingRef);
        if (booking) {
            this.showReceipt(
                booking.bookingReference,
                booking.serviceTitle,
                booking.vendorBusinessName,
                booking.totalAmount,
                booking.createdAt || booking.startTime,
                booking.meetingLink,
                booking.startTime,
                booking.endTime
            );
        }
    },

    async cancelBooking(bookingId) {
        if (!confirm('Are you sure you want to cancel this appointment? The time slot will be released back to the marketplace.')) {
            return;
        }

        try {
            await Api.request(`/bookings/${bookingId}/cancel`, {
                method: 'POST'
            });

            Api.showToast('Booking cancelled successfully! Slot restored to marketplace.', 'success');
            await this.loadMyBookings();
            if (window.App && App.loadServices) App.loadServices();
        } catch (error) {
            // Error handled
        }
    },

    showReceipt(ref, service, vendor, price, date, meetingLink, slotStartTime, slotEndTime) {
        const modal = document.getElementById('receipt-modal');
        if (!modal) return;

        const link = meetingLink && meetingLink !== 'undefined' ? meetingLink : 'https://meet.google.com/dae-zpiu-oau';
        
        const refEl = document.getElementById('receipt-ref');
        if (refEl) refEl.textContent = ref || 'N/A';

        const svcEl = document.getElementById('receipt-service');
        if (svcEl) svcEl.textContent = service || 'Service';

        const vendorEl = document.getElementById('receipt-vendor');
        if (vendorEl) vendorEl.textContent = vendor || 'Provider';

        // Real-Time Transaction Timestamp
        const dateEl = document.getElementById('receipt-date');
        if (dateEl) dateEl.textContent = date ? new Date(date).toLocaleString() : new Date().toLocaleString();

        // Reserved Slot Schedule
        const slotEl = document.getElementById('receipt-slot-time');
        if (slotEl) {
            if (slotStartTime && slotEndTime) {
                const sDate = new Date(slotStartTime);
                const eDate = new Date(slotEndTime);
                slotEl.textContent = `${sDate.toLocaleDateString()} (${sDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - ${eDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})})`;
            } else if (slotStartTime) {
                slotEl.textContent = new Date(slotStartTime).toLocaleString();
            } else {
                slotEl.textContent = 'Scheduled Slot';
            }
        }

        const priceEl = document.getElementById('receipt-price');
        if (priceEl) priceEl.textContent = `$${parseFloat(price || 0).toFixed(2)}`;

        const meetingLinkEl = document.getElementById('receipt-meeting-link');
        if (meetingLinkEl) {
            meetingLinkEl.href = link;
            meetingLinkEl.textContent = link;
        }

        modal.classList.add('active');
    },

    showReceiptModal(booking) {
        if (!booking) return;
        this.showReceipt(
            booking.bookingReference,
            booking.serviceTitle,
            booking.vendorBusinessName,
            booking.totalAmount,
            booking.createdAt || new Date(),
            booking.meetingLink,
            booking.startTime,
            booking.endTime
        );
    },

    closeReceiptModal() {
        document.getElementById('receipt-modal').classList.remove('active');
    }
};
