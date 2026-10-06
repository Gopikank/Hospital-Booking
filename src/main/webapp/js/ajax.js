/**
 * ============================================================================
 * AJAX Service Layer using Fetch API
 * Real-Time Hospital Appointment and Queue Management System
 * ============================================================================
 */

const HospitalAJAX = {

    /**
     * Generic asynchronous HTTP request wrapper
     */
    async request(url, options = {}) {
        const defaultHeaders = {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'application/json'
        };

        options.headers = { ...defaultHeaders, ...options.headers };

        try {
            const response = await fetch(url, options);
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('AJAX Request Error [' + url + ']:', error);
            throw error;
        }
    },

    /**
     * 1. Fetch current queue status (polled by patient & live queue)
     */
    async getQueueStatus(queueId = null, doctorId = null) {
        let url = contextPath + '/api/queue-status';
        const params = [];
        if (queueId) params.push('queueId=' + encodeURIComponent(queueId));
        if (doctorId) params.push('doctorId=' + encodeURIComponent(doctorId));
        if (params.length > 0) url += '?' + params.join('&');

        return this.request(url, { method: 'GET' });
    },

    /**
     * 2. Fetch available tokens for selected doctor and date
     */
    async getAvailableTokens(doctorId, date) {
        const url = `${contextPath}/api/available-tokens?doctorId=${encodeURIComponent(doctorId)}&date=${encodeURIComponent(date)}`;
        return this.request(url, { method: 'GET' });
    },

    /**
     * 3. Book token online
     */
    async bookToken(doctorId, date, tokenNumber, departmentName, rememberPref) {
        const url = contextPath + '/patient/book-token';
        const formData = new URLSearchParams();
        formData.append('doctorId', doctorId);
        formData.append('date', date);
        formData.append('tokenNumber', tokenNumber);
        if (departmentName) formData.append('departmentName', departmentName);
        if (rememberPref) formData.append('rememberPreference', 'true');

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 4. Check-in patient upon arrival
     */
    async checkIn(queueId) {
        const url = contextPath + '/patient/check-in';
        const formData = new URLSearchParams();
        formData.append('queueId', queueId);

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 5. Cancel token
     */
    async cancelToken(queueId) {
        const url = contextPath + '/patient/cancel-token';
        const formData = new URLSearchParams();
        formData.append('queueId', queueId);

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 6. Doctor: Call next patient
     */
    async doctorCallNext() {
        const url = contextPath + '/doctor/call-next';
        return this.request(url, { method: 'POST' });
    },

    /**
     * 7. Doctor: Start consultation
     */
    async doctorStartConsultation(queueId) {
        const url = contextPath + '/doctor/start-consultation';
        const formData = new URLSearchParams();
        formData.append('queueId', queueId);

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 8. Doctor: Complete consultation
     */
    async doctorCompleteConsultation(queueId) {
        const url = contextPath + '/doctor/complete-consultation';
        const formData = new URLSearchParams();
        formData.append('queueId', queueId);

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 9. Doctor: Mark absent
     */
    async doctorMarkAbsent(queueId) {
        const url = contextPath + '/doctor/mark-absent';
        const formData = new URLSearchParams();
        formData.append('queueId', queueId);

        return this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
    },

    /**
     * 10. Doctor: Fetch daily queue for doctor dashboard polling
     */
    async getDoctorQueue() {
        const url = contextPath + '/doctor/api/queue';
        return this.request(url, { method: 'GET' });
    },

    /**
     * 11. Admin: Fetch real-time dashboard statistics
     */
    async getAdminStats() {
        const url = contextPath + '/admin/dashboard-stats';
        return this.request(url, { method: 'GET' });
    },

    /**
     * 12. XML Service: Validate or Query XPath
     */
    async getXmlAction(action, query = '') {
        let url = `${contextPath}/admin/xml-config-action?action=${encodeURIComponent(action)}`;
        if (query) url += `&query=${encodeURIComponent(query)}`;
        return this.request(url, { method: 'GET' });
    }
};
