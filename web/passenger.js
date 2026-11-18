
const API = "/api/passenger";

// Read the token saved by the unified login page
function getToken() {
    return (
        sessionStorage.getItem("bm_token") ||
        sessionStorage.getItem("token") ||
        sessionStorage.getItem("authToken") ||
        ""
    );
}

let currentPage = "dashboard";
let searchResults = [];

async function api(endpoint, method = "GET", data = null) {
    const token = getToken();

    const options = {
        method,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        }
    };

    if (token) {
        options.headers.Authorization = "Bearer " + token;
    }

    if (data) {
        options.body = new URLSearchParams(data).toString();
    }

    const response = await fetch(API + endpoint, options);

    let result;

    try {
        result = await response.json();
    } catch {
        throw new Error("Invalid server response.");
    }

    if (!response.ok || result.success === false) {
        throw new Error(result.message || "Request failed.");
    }

    return result;
}

function showMessage(message) {
    const toast = document.getElementById("toast");

    toast.textContent = message;
    toast.classList.add("show");

    setTimeout(() => toast.classList.remove("show"), 3000);
}

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[char]);
}

function setPage(page) {
    currentPage = page;

    const titles = {
        dashboard: "Dashboard",
        search: "Search Buses",
        schedules: "View Schedules",
        prices: "Ticket Prices",
        book: "Book Seats",
        bookings: "My Bookings",
        tickets: "My Tickets",
        lost: "Report Lost Items",
        ratings: "Give Ratings",
        feedback: "View Feedback",
        complaints: "Submit Complaints"
    };

    document.getElementById("heading").textContent =
        titles[page] || "Dashboard";

    document.getElementById("nav").innerHTML = `
        ${navItem("dashboard", "Dashboard")}
        ${navItem("search", "Search Buses")}
        ${navItem("schedules", "View Schedules")}
        ${navItem("prices", "Ticket Prices")}
        ${navItem("book", "Book Seats")}
        ${navItem("bookings", "My Bookings")}
        ${navItem("tickets", "My Tickets")}
        ${navItem("lost", "Report Lost Items")}
        ${navItem("ratings", "Give Ratings")}
        ${navItem("feedback", "View Feedback")}
        ${navItem("complaints", "Submit Complaints")}
    `;

    document.querySelectorAll("[data-page]").forEach(button => {
        button.classList.toggle(
            "active",
            button.dataset.page === page
        );
    });

    loadPage(page);
}

function navItem(page, label) {
    return `
        <button class="nav-item ${currentPage === page ? "active" : ""}"
                data-page="${page}">
            ${label}
        </button>
    `;
}

async function loadPage(page) {
    const view = document.getElementById("view");
    view.innerHTML = `<p>Loading...</p>`;

    try {
        switch (page) {
            case "dashboard":
                await loadDashboard();
                break;

            case "search":
                showSearchPage("search");
                break;

            case "schedules":
                showSearchPage("schedules");
                break;

            case "prices":
                showSearchPage("prices");
                break;

            case "book":
                showBookingPage();
                break;

            case "bookings":
                await loadBookings();
                break;

            case "tickets":
                await loadTickets();
                break;

            case "lost":
                showLostItemForm();
                break;

            case "ratings":
                showRatingForm();
                break;

            case "feedback":
                showFeedbackPage();
                break;

            case "complaints":
                showComplaintForm();
                break;
        }
    } catch (error) {
        view.innerHTML = `<p>${escapeHTML(error.message)}</p>`;
    }
}

// DASHBOARD

async function loadDashboard() {
    const data = await api("/dashboard");

    document.getElementById("profileName").textContent =
        data.passengerName || "Passenger";

    document.getElementById("signedName").textContent =
        data.passengerName || "Passenger";

    document.getElementById("avatar").textContent =
        (data.passengerName || "P").charAt(0).toUpperCase();

    document.getElementById("view").innerHTML = `
        <h2>Welcome, ${escapeHTML(data.passengerName)}!</h2>
        <p>Here is your passenger account overview.</p>

        <div class="dashboard-cards">
            <div class="card">
                <h3>Passenger ID</h3>
                <h2>${escapeHTML(data.passengerId)}</h2>
            </div>

            <div class="card">
                <h3>My Bookings</h3>
                <h2>${escapeHTML(data.bookingCount)}</h2>
            </div>

            <div class="card">
                <h3>My Tickets</h3>
                <h2>${escapeHTML(data.ticketCount)}</h2>
            </div>
        </div>

        <div class="card">
            <h3>Account Information</h3>
            <p><b>Name:</b> ${escapeHTML(data.passengerName)}</p>
            <p><b>Email:</b> ${escapeHTML(data.email)}</p>
        </div>
    `;
}

// SEARCH, SCHEDULES AND PRICES

function showSearchPage(type) {
    const view = document.getElementById("view");

    const heading = type === "prices"
        ? "Find Ticket Prices"
        : type === "schedules"
            ? "Find Bus Schedules"
            : "Search Available Buses";

    view.innerHTML = `
        <div class="card">
            <h3>${heading}</h3>

            <form id="searchForm">
                <label>Starting Location</label>
                <input name="start" placeholder="e.g. Colombo">

                <label>Destination</label>
                <input name="destination" placeholder="e.g. Kandy">

                <label>Operating Day</label>
                <select name="day">
                    <option value="">Any day</option>
                    <option>Monday</option>
                    <option>Tuesday</option>
                    <option>Wednesday</option>
                    <option>Thursday</option>
                    <option>Friday</option>
                    <option>Saturday</option>
                    <option>Sunday</option>
                </select>

                <button type="submit">Search</button>
            </form>
        </div>

        <div id="searchResults"></div>
    `;

    document.getElementById("searchForm").addEventListener(
        "submit",
        event => {
            event.preventDefault();

            searchBuses(type, new FormData(event.target))
                .catch(error => showMessage(error.message));
        }
    );
}

async function searchBuses(type, formData) {
    const params = new URLSearchParams(formData).toString();

    const result = await api("/" + type + "?" + params);

    searchResults = result.buses || [];

    renderBusResults(searchResults, type);
}

function renderBusResults(buses, type) {
    const container = document.getElementById("searchResults");

    if (!buses.length) {
        container.innerHTML = `<p>No buses found.</p>`;
        return;
    }

    container.innerHTML = `
        <h3>Search Results</h3>

        <div class="results-list">
            ${buses.map(bus => `
                <div class="card">
                    <h3>${escapeHTML(bus.busNumber)}</h3>

                    <p><b>Route:</b>
                        ${escapeHTML(bus.startLocation)}
                        → ${escapeHTML(bus.destination)}
                    </p>

                    <p><b>Bus Type:</b> ${escapeHTML(bus.busType)}</p>
                    <p><b>Departure:</b> ${escapeHTML(bus.departure)}</p>
                    <p><b>Arrival:</b> ${escapeHTML(bus.arrival)}</p>
                    <p><b>Operating Days:</b>
                        ${escapeHTML(bus.operatingDays)}
                    </p>
                    <p><b>Ticket Price:</b>
                        Rs. ${escapeHTML(bus.ticketPrice)}
                    </p>

                    ${type === "search" || type === "schedules" ? `
                        <button data-book="${escapeHTML(bus.scheduleId)}">
                            Book This Bus
                        </button>
                    ` : ""}
                </div>
            `).join("")}
        </div>
    `;
}

// BOOKING

function showBookingPage() {
    document.getElementById("view").innerHTML = `
        <div class="card">
            <h3>Find a Bus to Book</h3>

            <form id="bookingSearchForm">
                <label>Starting Location</label>
                <input name="start" required>

                <label>Destination</label>
                <input name="destination" required>

                <label>Operating Day</label>
                <select name="day">
                    <option value="">Any day</option>
                    <option>Monday</option>
                    <option>Tuesday</option>
                    <option>Wednesday</option>
                    <option>Thursday</option>
                    <option>Friday</option>
                    <option>Saturday</option>
                    <option>Sunday</option>
                </select>

                <button type="submit">Find Buses</button>
            </form>
        </div>

        <div id="bookingResults"></div>
        <div id="seatSelection"></div>
    `;

    document.getElementById("bookingSearchForm").addEventListener(
        "submit",
        async event => {
            event.preventDefault();

            try {
                const params = new URLSearchParams(
                    new FormData(event.target)
                ).toString();

                const result = await api("/search?" + params);

                document.getElementById("bookingResults").innerHTML =
                    (result.buses || []).map(bus => `
                        <div class="card">
                            <h3>${escapeHTML(bus.busNumber)}</h3>

                            <p>${escapeHTML(bus.startLocation)}
                                → ${escapeHTML(bus.destination)}</p>

                            <p>Departure: ${escapeHTML(bus.departure)}</p>
                            <p>Price: Rs. ${escapeHTML(bus.ticketPrice)}</p>

                            <button data-book="${escapeHTML(bus.scheduleId)}">
                                Select Bus
                            </button>
                        </div>
                    `).join("") || "<p>No buses found.</p>";

            } catch (error) {
                showMessage(error.message);
            }
        }
    );
}

async function chooseBus(scheduleId) {
    const result = await api(
        "/seats?scheduleId=" + encodeURIComponent(scheduleId)
    );

    const seats = result.seats || [];

    document.getElementById("seatSelection").innerHTML = `
        <div class="card">
            <h3>Select Your Seat</h3>

            ${seats.length ? `
                <form id="bookingForm">
                    <input type="hidden" name="scheduleId"
                           value="${escapeHTML(scheduleId)}">

                    <label>Available Seat</label>

                    <select name="seatId" required>
                        ${seats.map(seat => `
                            <option value="${escapeHTML(seat.seatId)}">
                                ${escapeHTML(seat.seatNumber)}
                            </option>
                        `).join("")}
                    </select>

                    <label>Travel Date</label>
                    <input type="date" name="bookingDate" required>

                    <button type="submit">Confirm Booking</button>
                </form>
            ` : "<p>No available seats for this bus.</p>"}
        </div>
    `;

    const dateInput = document.querySelector(
        '#bookingForm input[name="bookingDate"]'
    );

    if (dateInput) {
        dateInput.min = new Date().toISOString().split("T")[0];
    }

    const form = document.getElementById("bookingForm");

    if (form) {
        form.addEventListener("submit", async event => {
            event.preventDefault();

            const data = Object.fromEntries(new FormData(form));

            try {
                const result = await api("/book", "POST", data);

                showMessage(
                    (result.message || "Booking successful.") +
                    (result.ticketNumber
                        ? " Ticket: " + result.ticketNumber
                        : "")
                );

                setPage("tickets");

            } catch (error) {
                showMessage(error.message);
            }
        });
    }
}

// MY BOOKINGS

async function loadBookings() {
    const data = await api("/bookings");
    const bookings = data.bookings || [];

    document.getElementById("view").innerHTML = `
        <h3>My Bookings</h3>

        ${bookings.length ? bookings.map(b => `
            <div class="card">
                <h3>Booking #${escapeHTML(b.bookingId)}</h3>

                <p><b>Bus:</b> ${escapeHTML(b.busNumber)}</p>

                <p><b>Route:</b>
                    ${escapeHTML(b.startLocation)} →
                    ${escapeHTML(b.destination)}
                </p>

                <p><b>Seat:</b> ${escapeHTML(b.seatNumber)}</p>
                <p><b>Date:</b> ${escapeHTML(b.bookingDate)}</p>
                <p><b>Status:</b> ${escapeHTML(b.status)}</p>
            </div>
        `).join("") : "<p>You have no bookings yet.</p>"}
    `;
}

// MY TICKETS

async function loadTickets() {
    const data = await api("/tickets");
    const tickets = data.tickets || [];

    document.getElementById("view").innerHTML = `
        <h3>My Tickets</h3>

        ${tickets.length ? tickets.map(t => `
            <div class="card">
                <h3>${escapeHTML(t.ticketNumber)}</h3>

                <p><b>Bus:</b> ${escapeHTML(t.busNumber)}</p>

                <p><b>Route:</b>
                    ${escapeHTML(t.startLocation)} →
                    ${escapeHTML(t.destination)}
                </p>

                <p><b>Seat:</b> ${escapeHTML(t.seatNumber)}</p>
                <p><b>Departure:</b> ${escapeHTML(t.departure)}</p>
                <p><b>Fare:</b> Rs. ${escapeHTML(t.fare)}</p>
                <p><b>Issued:</b> ${escapeHTML(t.issueDate)}</p>
            </div>
        `).join("") : "<p>You have no tickets yet.</p>"}
    `;
}

// LOST ITEMS

function showLostItemForm() {
    document.getElementById("view").innerHTML = `
        <div class="card">
            <h3>Report a Lost Item</h3>

            <form id="lostForm">
                <label>Item Description</label>

                <textarea name="description" required
                    placeholder="Describe the lost item"></textarea>

                <label>Travel Date</label>
                <input type="date" name="travelDate" required>

                <button type="submit">Submit Report</button>
            </form>
        </div>
    `;
}

// RATINGS

function showRatingForm() {
    document.getElementById("view").innerHTML = `
        <div class="card">
            <h3>Rate a Bus</h3>

            <form id="ratingForm">
                <label>Bus Registration Number</label>

                <input name="busNumber" required
                    placeholder="e.g. NA-1234">

                <label>Rating</label>

                <select name="rating" required>
                    <option value="">Select rating</option>
                    <option value="5">5 - Excellent</option>
                    <option value="4">4 - Very Good</option>
                    <option value="3">3 - Good</option>
                    <option value="2">2 - Poor</option>
                    <option value="1">1 - Very Poor</option>
                </select>

                <label>Comment</label>

                <textarea name="comment"
                    placeholder="Write your feedback"></textarea>

                <button type="submit">Submit Rating</button>
            </form>
        </div>
    `;
}

// FEEDBACK

function showFeedbackPage() {
    document.getElementById("view").innerHTML = `
        <div class="card">
            <h3>View Bus Feedback</h3>

            <form id="feedbackForm">
                <label>Bus Registration Number</label>

                <input name="busNumber" required
                    placeholder="e.g. NA-1234">

                <button type="submit">View Feedback</button>
            </form>
        </div>

        <div id="feedbackResults"></div>
    `;
}

// COMPLAINTS

function showComplaintForm() {
    document.getElementById("view").innerHTML = `
        <div class="card">
            <h3>Submit a Complaint</h3>

            <form id="complaintForm">
                <label>Complaint Description</label>

                <textarea name="description" required
                    placeholder="Describe your complaint"></textarea>

                <button type="submit">Submit Complaint</button>
            </form>
        </div>
    `;
}

// NAVIGATION AND BUTTON EVENTS

document.addEventListener("click", event => {
    const pageButton = event.target.closest("[data-page]");

    if (pageButton) {
        setPage(pageButton.dataset.page);
        return;
    }

    const bookButton = event.target.closest("[data-book]");

    if (bookButton) {
        chooseBus(bookButton.dataset.book).catch(error =>
            showMessage(error.message)
        );
    }
});

// FORM SUBMISSIONS

document.addEventListener("submit", async event => {
    const form = event.target;

    const actions = {
        lostForm: "/lost-items",
        ratingForm: "/ratings",
        complaintForm: "/complaints"
    };

    if (actions[form.id]) {
        event.preventDefault();

        try {
            const data = Object.fromEntries(new FormData(form));

            const result = await api(
                actions[form.id],
                "POST",
                data
            );

            showMessage(result.message || "Submitted successfully.");
            form.reset();

        } catch (error) {
            showMessage(error.message);
        }
    }

    if (form.id === "feedbackForm") {
        event.preventDefault();

        try {
            const data = new FormData(form);
            const params = new URLSearchParams(data).toString();

            const result = await api("/feedback?" + params);
            const feedback = result.feedback || [];

            document.getElementById("feedbackResults").innerHTML =
                feedback.length ? feedback.map(item => `
                    <div class="card">
                        <h3>${escapeHTML(item.passengerName)}</h3>
                        <p>Rating: ${escapeHTML(item.rating)} / 5</p>
                        <p>${escapeHTML(item.comment)}</p>
                    </div>
                `).join("") : "<p>No feedback found for this bus.</p>";

        } catch (error) {
            showMessage(error.message);
        }
    }
});

// LOGOUT

function logout() {
    sessionStorage.removeItem("bm_token");
    sessionStorage.removeItem("bm_user");
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("authToken");
    sessionStorage.removeItem("currentUser");

    window.location.href = "/index.html";
}

// START APPLICATION

if (!getToken()) {
    window.location.href = "/index.html";
} else {
    setPage("dashboard");
}
