/**
 * ============================================================================
 * Real-Time Queue & DOM Manipulation Engine
 * Real-Time Hospital Appointment and Queue Management System
 * ============================================================================
 */

const QueueManager = {
    pollingInterval: null,
    pollFrequencyMs: 3000, // 3 seconds polling as required in section 12
    lastNotifiedToken: null,

    /**
     * Start real-time polling for patient dashboard
     */
    startPatientPolling(queueId = null) {
        this.updatePatientDashboard(queueId);
        if (this.pollingInterval) clearInterval(this.pollingInterval);
        this.pollingInterval = setInterval(() => {
            this.updatePatientDashboard(queueId);
        }, this.pollFrequencyMs);
    },

    /**
     * DOM Update for patient dashboard
     */
    async updatePatientDashboard(queueId) {
        try {
            const resp = await HospitalAJAX.getQueueStatus(queueId);
            if (!resp || !resp.success || !resp.data) return;

            const d = resp.data;

            // Update timestamp
            const updateElem = document.getElementById("lastUpdated");
            if (updateElem && d.lastUpdated) {
                updateElem.innerText = d.lastUpdated;
            }

            if (!d.hasActiveToken) {
                const noTokenCard = document.getElementById("noActiveTokenCard");
                const activeCard = document.getElementById("activeTokenCard");
                if (noTokenCard) noTokenCard.style.display = "block";
                if (activeCard) activeCard.style.display = "none";
                return;
            }

            // Visible dynamic DOM updates as required in Section 13
            const currentTokenElem = document.getElementById("currentToken");
            if (currentTokenElem && d.currentToken !== undefined) {
                currentTokenElem.innerText = d.currentToken;
            }

            const patientTokenElem = document.getElementById("patientToken");
            if (patientTokenElem && d.tokenNumber !== undefined) {
                patientTokenElem.innerText = d.tokenNumber;
            }

            const peopleAheadElem = document.getElementById("peopleAhead");
            if (peopleAheadElem && d.peopleAhead !== undefined) {
                peopleAheadElem.innerText = d.peopleAhead;
            }

            const waitTimeElem = document.getElementById("estimatedWait");
            if (waitTimeElem && d.estimatedWaitMinutes !== undefined) {
                waitTimeElem.innerText = d.estimatedWaitMinutes + " mins";
            }

            const statusBadge = document.getElementById("queueStatusBadge");
            if (statusBadge && d.status) {
                statusBadge.innerText = d.status;
                statusBadge.className = "badge badge-" + d.status.toLowerCase();
            }

            // Notification Banner updates
            const banner = document.getElementById("notificationBanner");
            const notifyText = document.getElementById("notificationText");
            if (banner && notifyText && d.notificationMessage) {
                notifyText.innerText = d.notificationMessage;

                if (d.isCalled) {
                    banner.className = "notification-banner called";
                    // Play subtle alert tone if first time called
                    if (this.lastNotifiedToken !== d.tokenNumber) {
                        this.playAlertTone();
                        this.lastNotifiedToken = d.tokenNumber;
                    }
                } else if (d.isNext) {
                    banner.className = "notification-banner next";
                } else {
                    banner.className = "notification-banner waiting";
                }
            }

            // Button state toggles
            const checkInBtn = document.getElementById("btnCheckIn");
            if (checkInBtn) {
                checkInBtn.style.display = (d.status === "BOOKED") ? "inline-flex" : "none";
            }

            const cancelBtn = document.getElementById("btnCancelToken");
            if (cancelBtn) {
                const canCancel = (d.status === "BOOKED" || d.status === "WAITING");
                cancelBtn.style.display = canCancel ? "inline-flex" : "none";
            }

        } catch (e) {
            console.error("Queue polling update error:", e);
        }
    },

    /**
     * Start doctor dashboard polling
     */
    startDoctorPolling() {
        this.updateDoctorDashboard();
        if (this.pollingInterval) clearInterval(this.pollingInterval);
        this.pollingInterval = setInterval(() => {
            this.updateDoctorDashboard();
        }, this.pollFrequencyMs);
    },

    /**
     * Doctor dashboard DOM updates
     */
    async updateDoctorDashboard() {
        try {
            const resp = await HospitalAJAX.getDoctorQueue();
            if (!resp || !resp.success || !resp.data) return;

            const d = resp.data;

            // Update stats counters
            const updateTxt = (id, val) => {
                const el = document.getElementById(id);
                if (el) el.innerText = val !== undefined ? val : 0;
            };

            updateTxt("docWaitingCount", d.waitingCount);
            updateTxt("docCompletedCount", d.completedCount);
            updateTxt("docAbsentCount", d.absentCount);
            updateTxt("docTotalPatients", d.totalPatients);
            updateTxt("docLastUpdated", d.lastUpdated);

            // Update Current Serving Card
            const servingCard = document.getElementById("currentServingContent");
            const btnStart = document.getElementById("btnStartConsultation");
            const btnComplete = document.getElementById("btnCompleteConsultation");
            const btnAbsent = document.getElementById("btnMarkAbsent");
            const btnCallNext = document.getElementById("btnCallNext");

            if (d.currentServing) {
                const cs = d.currentServing;
                if (servingCard) {
                    servingCard.innerHTML = `
                        <div class="card-value" style="color: var(--primary);">Token ${cs.tokenNumber}</div>
                        <div style="font-size: 1.1rem; font-weight: 700; margin-top: 0.25rem;">${cs.patientName}</div>
                        <div class="card-subtext">Source: <span class="badge badge-${cs.bookingSource.toLowerCase()}">${cs.bookingSource}</span> | Status: <span class="badge badge-${cs.status.toLowerCase()}">${cs.status}</span></div>
                    `;
                }
                if (btnStart) {
                    btnStart.dataset.queueId = cs.queueId;
                    btnStart.dataset.token = cs.tokenNumber;
                    btnStart.style.display = (cs.status === 'CALLED') ? 'inline-flex' : 'none';
                }
                if (btnComplete) {
                    btnComplete.dataset.queueId = cs.queueId;
                    btnComplete.dataset.token = cs.tokenNumber;
                    btnComplete.style.display = (cs.status === 'IN_CONSULTATION' || cs.status === 'CALLED') ? 'inline-flex' : 'none';
                }
                if (btnAbsent) {
                    btnAbsent.dataset.queueId = cs.queueId;
                    btnAbsent.dataset.token = cs.tokenNumber;
                    btnAbsent.style.display = (cs.status === 'CALLED') ? 'inline-flex' : 'none';
                }
                if (btnCallNext) btnCallNext.disabled = (cs.status === 'IN_CONSULTATION');
            } else {
                if (servingCard) {
                    servingCard.innerHTML = `
                        <div class="card-value" style="color: var(--text-muted);">None</div>
                        <div class="card-subtext">No patient currently called or in consultation.</div>
                    `;
                }
                if (btnStart) btnStart.style.display = 'none';
                if (btnComplete) btnComplete.style.display = 'none';
                if (btnAbsent) btnAbsent.style.display = 'none';
                if (btnCallNext) btnCallNext.disabled = false;
            }

            // Update Waiting Patients Table
            const waitingBody = document.getElementById("waitingListBody");
            if (waitingBody) {
                if (!d.waitingList || d.waitingList.length === 0) {
                    waitingBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No patients currently waiting in line.</td></tr>`;
                } else {
                    waitingBody.innerHTML = d.waitingList.map((item, idx) => `
                        <tr>
                            <td><strong>${item.tokenNumber}</strong></td>
                            <td>${item.patientName}</td>
                            <td><span class="badge badge-${item.bookingSource.toLowerCase()}">${item.bookingSource}</span></td>
                            <td><span class="badge badge-waiting">WAITING</span></td>
                            <td>${idx === 0 ? '<span style="color: var(--warning); font-weight:700;">NEXT</span>' : (idx + 1) + ' in line'}</td>
                        </tr>
                    `).join('');
                }
            }

        } catch (e) {
            console.error("Doctor polling error:", e);
        }
    },

    /**
     * Start Admin dashboard polling
     */
    startAdminPolling() {
        this.updateAdminDashboard();
        if (this.pollingInterval) clearInterval(this.pollingInterval);
        this.pollingInterval = setInterval(() => {
            this.updateAdminDashboard();
        }, 5000);
    },

    async updateAdminDashboard() {
        try {
            const resp = await HospitalAJAX.getAdminStats();
            if (!resp || !resp.success || !resp.data) return;

            const d = resp.data;
            const setTxt = (id, val) => {
                const el = document.getElementById(id);
                if (el) el.innerText = val !== undefined ? val : 0;
            };

            setTxt("statTotalPatients", d.totalPatients);
            setTxt("statOnlineBookings", d.onlineBookings);
            setTxt("statWalkinTokens", d.walkinTokens);
            setTxt("statWaiting", d.waitingPatients);
            setTxt("statInConsultation", d.inConsultation);
            setTxt("statCompleted", d.completed);
            setTxt("statCancelled", d.cancelled);
            setTxt("statAbsent", d.absent);
            setTxt("statActiveDoctors", d.activeDoctors);

            const deptBody = document.getElementById("adminDeptStatsBody");
            if (deptBody && d.departmentStats) {
                deptBody.innerHTML = d.departmentStats.map(ds => `
                    <tr>
                        <td><strong>${ds.departmentName}</strong></td>
                        <td><span class="badge badge-waiting">${ds.waitingCount}</span></td>
                        <td>${ds.totalCount}</td>
                    </tr>
                `).join('');
            }
        } catch (e) {
            console.error("Admin polling error:", e);
        }
    },

    /**
     * Web Audio API synthesized gentle chime when called
     */
    playAlertTone() {
        try {
            const ctx = new (window.AudioContext || window.webkitAudioContext)();
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();

            osc.type = "sine";
            osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
            osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5

            gain.gain.setValueAtTime(0.15, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.5);

            osc.connect(gain);
            gain.connect(ctx.destination);

            osc.start();
            osc.stop(ctx.currentTime + 0.5);
        } catch (e) {
            // AudioContext not permitted without prior user gesture
        }
    },

    /**
     * Cookie helper
     */
    getCookie(name) {
        const matches = document.cookie.match(new RegExp(
            "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
        ));
        return matches ? decodeURIComponent(matches[1]) : null;
    }
};
