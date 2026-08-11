const Calendar = {
    selectedSlotId: null,
    currentService: null,
    activeBookingHold: null,

    async loadSlotsForService(serviceId, serviceObj) {
        this.currentService = serviceObj;
        this.selectedSlotId = null;

        const grid = document.getElementById('modal-slots-grid');
        grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1;">Checking available slot schedules...</div>';

        try {
            const slots = await Api.request(`/services/${serviceId}/slots`);

            if (!slots || slots.length === 0) {
                grid.innerHTML = '<div style="color: var(--text-secondary); text-align: center; grid-column: 1/-1;">No open availability slots for this service right now. Please check back soon!</div>';
                return;
            }

            grid.innerHTML = slots.map(slot => {
                const dateObj = new Date(slot.startTime);
                const dateStr = dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric', weekday: 'short' });
                const timeStr = dateObj.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });

                return `
                    <div class="slot-item" id="slot-item-${slot.id}" onclick="Calendar.selectSlot(${slot.id})">
                        <div style="font-weight: 700; font-size: 0.85rem; color: white;">${dateStr}</div>
                        <div style="font-size: 0.95rem; color: var(--accent-secondary); margin-top: 0.2rem;">${timeStr}</div>
                        <div style="font-size: 0.7rem; color: var(--accent-success); margin-top: 0.3rem;">● Available</div>
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
        btn.textContent = '🔒 Locking Slot in DB...';

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
            Api.showToast('🔒 Pessimistic Lock acquired! Open card checkout...', 'success');

            btn.disabled = false;
            btn.textContent = 'Proceed to Payment Gateway';

            App.closeBookingModal();
            this.openPaymentGatewayModal(bookingResponse);

        } catch (error) {
            btn.disabled = false;
            btn.textContent = 'Proceed to Payment Gateway';
        }
    },

    openPaymentGatewayModal(booking) {
        document.getElementById('pay-service-title').textContent = booking.serviceTitle;
        document.getElementById('pay-vendor-name').textContent = `Provider: ${booking.vendorBusinessName}`;
        document.getElementById('pay-total-price').textContent = `$${booking.totalAmount.toFixed(2)}`;
        document.getElementById('pay-booking-ref').textContent = booking.bookingReference.substring(0, 8) + '...';

        document.getElementById('payment-modal').classList.add('active');
    },

    closePaymentGatewayModal() {
        document.getElementById('payment-modal').classList.remove('active');
    },

    formatCardNumber(input) {
        let value = input.value.replace(/\D/g, '');
        value = value.substring(0, 16);
        const formatted = value.match(/.{1,4}/g)?.join(' ') || '';
        input.value = formatted;

        const display = document.getElementById('card-num-display');
        if (display) display.textContent = formatted || '•••• •••• •••• ••••';

        // Detect brand
        const brandDisplay = document.getElementById('card-brand-display');
        if (brandDisplay) {
            if (value.startsWith('4')) brandDisplay.textContent = 'VISA';
            else if (value.startsWith('5')) brandDisplay.textContent = 'MASTERCARD';
            else if (value.startsWith('3')) brandDisplay.textContent = 'AMEX';
            else brandDisplay.textContent = 'CARD';
        }
    },

    updateCardName(input) {
        const display = document.getElementById('card-name-display');
        if (display) display.textContent = (input.value || 'CARDHOLDER NAME').toUpperCase();
    },

    async processCardPayment(event) {
        event.preventDefault();

        if (!this.activeBookingHold) {
            Api.showToast('No active booking hold found', 'danger');
            return;
        }

        const btn = document.getElementById('pay-process-btn');
        btn.disabled = true;
        btn.textContent = '⚡ Processing Stripe Sandbox Payment...';

        try {
            const confirmed = await Api.request('/bookings/confirm-mock', {
                method: 'POST',
                body: JSON.stringify({ bookingReference: this.activeBookingHold.bookingReference })
            });

            btn.disabled = false;
            btn.textContent = 'Pay & Complete Booking';
            this.closePaymentGatewayModal();

            // Show instant receipt modal
            Profile.showReceipt(
                confirmed.bookingReference,
                confirmed.serviceTitle,
                confirmed.vendorBusinessName,
                confirmed.totalAmount,
                confirmed.createdAt || new Date(),
                confirmed.meetingLink,
                confirmed.startTime,
                confirmed.endTime
            );

            Api.showToast('🎉 Payment Succeeded! Booking Confirmed!', 'success');
            App.loadServices();

        } catch (error) {
            btn.disabled = false;
            btn.textContent = 'Pay & Complete Booking';
        }
    }
};
