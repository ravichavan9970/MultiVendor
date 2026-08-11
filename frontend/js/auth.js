const Auth = {
    pendingRegisterEmail: null,

    init() {
        this.updateNavUI();
    },

    updateNavUI() {
        const user = Api.getUser();
        const authButtons = document.getElementById('auth-buttons');
        const userProfile = document.getElementById('user-profile');
        const userDisplayName = document.getElementById('user-display-name');

        if (user && Api.getToken()) {
            if (authButtons) authButtons.style.display = 'none';
            if (userProfile) userProfile.style.display = 'flex';
            if (userDisplayName) userDisplayName.textContent = `${user.fullName}`;
        } else {
            if (authButtons) authButtons.style.display = 'flex';
            if (userProfile) userProfile.style.display = 'none';
        }
    },

    showLoginModal() {
        document.getElementById('login-modal').classList.add('active');
    },

    closeLoginModal() {
        document.getElementById('login-modal').classList.remove('active');
    },

    showRegisterModal() {
        document.getElementById('register-modal').classList.add('active');
    },

    closeRegisterModal() {
        document.getElementById('register-modal').classList.remove('active');
    },

    showOtpModal(email, demoCode) {
        this.pendingRegisterEmail = email;
        document.getElementById('otp-target-display').textContent = email;
        document.getElementById('otp-demo-hint-code').textContent = demoCode;

        // Clear 6 digit boxes
        const digits = document.querySelectorAll('.otp-digit');
        digits.forEach(d => d.value = '');
        
        document.getElementById('otp-modal').classList.add('active');
        if (digits[0]) digits[0].focus();
    },

    closeOtpModal() {
        document.getElementById('otp-modal').classList.remove('active');
    },

    handleOtpDigitInput(input, index) {
        if (input.value && index < 5) {
            const digits = document.querySelectorAll('.otp-digit');
            if (digits[index + 1]) digits[index + 1].focus();
        }
    },

    handleOtpKeyDown(event, index) {
        if (event.key === 'Backspace') {
            const digits = document.querySelectorAll('.otp-digit');
            if (!digits[index].value && index > 0) {
                digits[index - 1].focus();
            }
        }
    },

    async handleLogin(event) {
        event.preventDefault();
        const identifier = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            const data = await Api.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email: identifier, password })
            });

            Api.setToken(data.token);
            Api.setUser({
                id: data.userId,
                email: data.email,
                fullName: data.fullName,
                role: data.role,
                vendorId: data.vendorId
            });

            Api.showToast(`Welcome back, ${data.fullName}!`, 'success');
            this.closeLoginModal();
            this.updateNavUI();
            
            if (data.role === 'VENDOR') {
                window.location.href = 'vendor-dashboard.html';
            }
        } catch (error) {
            // Handled
        }
    },

    async handleRegister(event) {
        event.preventDefault();
        const fullName = document.getElementById('reg-name').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;
        const role = document.getElementById('reg-role').value;
        const cc = document.getElementById('reg-country-code')?.value || '+91';
        const num = (document.getElementById('reg-phone')?.value || '').trim();
        const phoneNumber = num ? `${cc} ${num}` : '';

        try {
            const response = await Api.request('/auth/register-initiate', {
                method: 'POST',
                body: JSON.stringify({ fullName, email, password, role, phoneNumber })
            });

            this.closeRegisterModal();
            this.showOtpModal(email, response.demoOtpCode);

            Api.showToast(`📲 OTP Code [ ${response.demoOtpCode} ] sent to Email & Mobile!`, 'info');

        } catch (error) {
            // Handled
        }
    },

    async handleVerifyOtp(event) {
        event.preventDefault();
        const digits = document.querySelectorAll('.otp-digit');
        let otpCode = '';
        digits.forEach(d => otpCode += d.value.trim());

        if (otpCode.length !== 6) {
            Api.showToast('Please enter all 6 digits of the OTP code', 'danger');
            return;
        }

        const btn = document.getElementById('otp-verify-btn');
        btn.disabled = true;
        btn.textContent = 'Verifying OTP & Saving to DB...';

        try {
            const data = await Api.request('/auth/register-verify-otp', {
                method: 'POST',
                body: JSON.stringify({ email: this.pendingRegisterEmail, otpCode })
            });

            Api.setToken(data.token);
            Api.setUser({
                id: data.userId,
                email: data.email,
                fullName: data.fullName,
                role: data.role,
                vendorId: data.vendorId
            });

            btn.disabled = false;
            btn.textContent = 'Verify & Create Account';
            this.closeOtpModal();

            Api.showToast('🎉 OTP Verified! Account registered & credentials saved in database!', 'success');
            this.updateNavUI();

            if (data.role === 'VENDOR') {
                window.location.href = 'vendor-dashboard.html';
            }

        } catch (error) {
            btn.disabled = false;
            btn.textContent = 'Verify & Create Account';
        }
    },

    logout() {
        Api.clearAuth();
        Api.showToast('Logged out successfully', 'info');
        this.updateNavUI();
        window.location.href = 'index.html';
    }
};

document.addEventListener('DOMContentLoaded', () => Auth.init());
