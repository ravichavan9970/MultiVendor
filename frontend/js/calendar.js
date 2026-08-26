const Calendar = {
    selectedSlotId: null,
    currentService: null,
    activeBookingHold: null,

    async loadSlotsForService(serviceId, serviceObj) {
        this.currentService = serviceObj;
        this.selectedSlotId = null;

        const grid = document.getElementById('modal-slots-grid');
        grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1; padding: 1.5rem 0;">Checking available schedule...</div>';

        try {
            const slots = await Api.request(`/services/${serviceId}/slots`);

            if (!slots || slots.length === 0) {
                grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1; padding: 1.5rem 0;">No open availability slots for this service right now. Please check back soon!</div>';
                return;
            }

            grid.innerHTML = slots.map(slot => {
                const dateObj = new Date(slot.startTime);
                const dateStr = dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric', weekday: 'short' });
                const timeStr = dateObj.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });

                return `
                    <div class="slot-item" id="slot-item-${slot.id}" onclick="Calendar.selectSlot(${slot.id})">
                        <div style="font-weight: 700; font-size: 0.85rem; color: white;">${dateStr}</div>
                        <div style="font-size: 0.95rem; color: var(--brand-violet); margin-top: 0.2rem;">${timeStr}</div>
                        <div style="font-size: 0.7rem; color: var(--brand-emerald); margin-top: 0.3rem;">● Available</div>
                    </div>
                `;
            }).join('');
        } catch (error) {
            grid.innerHTML = '<div style="color: var(--accent-danger); text-align: center; grid-column: 1/-1;">Failed to load slots.</div>';
        }
    },

    selectSlot(slotId) {
        this.selectedSlotId = slotId;
        document.querySelectorAll('.slot-item').forEach(el => el.classList.remove('selected'));
        const target = document.getElementById(`slot-item-${slotId}`);
        if (target) target.classList.add('selected');
    },

    async confirmBookingHold() {
        if (!Api.getToken()) {
            Api.showToast('Please log in or sign up before reserving a slot!', 'info');
            Auth.showLoginModal();
            return;
        }

        if (!this.selectedSlotId) {
            Api.showToast('Please click to select an available time slot first!', 'info');
            return;
        }

        const btn = document.getElementById('confirm-hold-btn');
        btn.disabled = true;
        btn.innerHTML = '🔒 Locking Slot & Generating UPI QR...';

        try {
            const customerNotes = document.getElementById('booking-customer-notes')?.value || '';
            const bookingResponse = await Api.request('/bookings/hold', {
                method: 'POST',
                body: JSON.stringify({
                    slotId: this.selectedSlotId,
                    customerNotes: customerNotes
                })
            });

            this.activeBookingHold = bookingResponse;
            Api.showToast('🔒 Slot locked! Scan UPI QR to complete payment.', 'success');

            btn.disabled = false;
            btn.innerHTML = 'Proceed to UPI / QR Code Payment 📲';

            App.closeBookingModal();
            this.openUpiModal(bookingResponse);

        } catch (error) {
            btn.disabled = false;
            btn.innerHTML = 'Proceed to UPI / QR Code Payment 📲';
        }
    },

    openUpiModal(booking) {
        document.getElementById('upi-service-title').textContent = booking.serviceTitle;
        document.getElementById('upi-vendor-name').textContent = `Provider: ${booking.vendorBusinessName}`;
        document.getElementById('upi-booking-ref').textContent = booking.bookingReference.substring(0, 8) + '...';
        
        const price = booking.totalAmount.toFixed(2);
        document.getElementById('upi-total-price').textContent = `$${price}`;

        // UPI URI Format: upi://pay?pa=VPA&pn=NAME&am=AMOUNT&cu=INR&tn=NOTE
        const upiVpa = '7447661921@hdfc';
        const payeeName = 'RAVINDRA LAXMAN CHAVAN';
        const upiNote = `Booking-${booking.bookingReference.substring(0, 8)}`;
        const upiUri = `upi://pay?pa=${encodeURIComponent(upiVpa)}&pn=${encodeURIComponent(payeeName)}&am=${price}&cu=INR&tn=${encodeURIComponent(upiNote)}`;
        
        // Use user's official HDFC QR Code image
        const qrImg = document.getElementById('upi-qr-img');
        if (qrImg) {
            qrImg.src = 'img/upi-qr.jpg';
        }

        // Set app deep links
        document.getElementById('upi-gpay-btn').href = upiUri;
        document.getElementById('upi-phonepe-btn').href = upiUri;
        document.getElementById('upi-paytm-btn').href = upiUri;

        // Clear previous UTR inputs
        document.getElementById('upi-utr-input').value = '';
        if (document.getElementById('upi-notes-input')) document.getElementById('upi-notes-input').value = '';

        document.getElementById('upi-payment-modal').classList.add('active');
    },

    closeUpiModal() {
        document.getElementById('upi-payment-modal').classList.remove('active');
    },

    copyUpiId() {
        navigator.clipboard.writeText('7447661921@hdfc');
        Api.showToast('📋 UPI ID copied to clipboard: 7447661921@hdfc', 'info');
    },

    async submitUtrNumber(event) {
        event.preventDefault();
        const utrNumber = document.getElementById('upi-utr-input').value.trim();
        const notes = document.getElementById('upi-notes-input')?.value.trim() || '';

        if (!utrNumber || utrNumber.length < 6) {
            Api.showToast('Please enter a valid 12-digit UTR / Transaction Reference Number!', 'warning');
            return;
        }

        const btn = document.getElementById('upi-submit-btn');
        btn.disabled = true;
        btn.innerHTML = '⏳ Submitting UTR for Admin Verification...';

        try {
            const resp = await Api.request('/bookings/submit-utr', {
                method: 'POST',
                body: JSON.stringify({
                    bookingReference: this.activeBookingHold.bookingReference,
                    utrNumber: utrNumber,
                    notes: notes
                })
            });

            btn.disabled = false;
            btn.innerHTML = '🚀 Submit UTR for Admin Verification';
            this.closeUpiModal();

            Api.showToast('🎉 UTR Submitted! Admin will verify and confirm your booking.', 'success');
            
            // Show receipt/pending modal
            Profile.showReceipt(
                resp.bookingReference,
                resp.serviceTitle,
                resp.vendorBusinessName,
                resp.totalAmount,
                resp.createdAt || new Date(),
                resp.meetingLink,
                resp.startTime,
                resp.endTime
            );
        } catch (error) {
            btn.disabled = false;
            btn.innerHTML = '🚀 Submit UTR for Admin Verification';
        }
    }
};
