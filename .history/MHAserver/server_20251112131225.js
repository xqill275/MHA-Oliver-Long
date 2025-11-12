require("dotenv").config();
const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const bodyParser = require("body-parser");

const app = express();
app.use(cors());
app.use(bodyParser.json());

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASS,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT
});

db.connect((err) => {
    if (err) {
        console.error("❌ Database connection failed:", err);
        return;
    }
    console.log("✅ Connected to MySQL database!");
});

app.get("/", (req, res) => {
    res.send("MHA API is running");
});


// USERS

app.get("/api/users", (req, res) => {
    db.query("SELECT * FROM users", (err, results) => {
        if (err) return res.status(500).json({ error: "Database error" });
        res.json(results);
    });
});

app.get("/api/user", (req, res) => {
    const userId = req.query.id;
    if (!userId) return res.status(400).json({ error: "Missing user ID in query" });

    db.query("SELECT * FROM users WHERE UID = ?", [userId], (err, results) => {
        if (err) return res.status(500).json({ error: "Database query error" });
        if (results.length === 0) return res.status(404).json({ error: "User not found" });
        res.json(results[0]);
    });
});

app.put("/api/user/role", (req, res) => {
    const { uid, role } = req.body;
    if (!uid || !role) return res.status(400).json({ error: "Missing uid or role" });

    db.query("UPDATE users SET Role = ? WHERE UID = ?", [role, uid], (err, result) => {
        if (err) return res.status(500).json({ error: "Database update failed" });
        if (result.affectedRows === 0) return res.status(404).json({ error: "User not found" });
        res.json({ message: "Role updated successfully!" });
    });
});

app.post("/api/users", (req, res) => {
    const { FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash } = req.body;
    const sql = `
        INSERT INTO users (FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;
    db.query(sql, [FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash], (err, result) => {
        if (err) return res.status(500).json({ error: "Insert failed" });
        res.json({ message: "User added successfully", id: result.insertId });
    });
});


// HOSPITALS

app.post("/api/hospitals", (req, res) => {
    const { name, city, postcode } = req.body;
    if (!name || !city || !postcode) return res.status(400).json({ error: "Missing required fields" });

    db.query("INSERT INTO hospitals (name, city, postcode) VALUES (?, ?, ?)",
        [name, city, postcode],
        (err, result) => {
            if (err) return res.status(500).json({ error: "Insert failed" });
            res.json({ message: "Hospital added successfully", id: result.insertId });
        });
});

app.get("/api/hospitals", (req, res) => {
    db.query("SELECT * FROM hospitals", (err, results) => {
        if (err) return res.status(500).json({ error: "Database error" });
        res.json(results);
    });
});


app.get("/api/hospitals/:id", (req, res) => {
    const hospitalID = req.params.id;

    db.query("SELECT * FROM hospitals WHERE hospitalID = ?", [hospitalID], (err, results) => {
        if (err) {
            console.error("Database query failed:", err);
            return res.status(500).json({ error: "Database error" });
        }

        if (results.length === 0) {
            return res.status(404).json({ error: "Hospital not found" });
        }

        res.json(results[0]);
    });
});

// APPOINTMENTS

// Get all appointments
app.get("/api/appointments", (req, res) => {
    db.query("SELECT * FROM appointments", (err, results) => {
        if (err) return res.status(500).json({ error: "Database error" });
        res.json(results);
    });
});

// Add a new appointment slot (available date)
app.post("/api/appointments/add", (req, res) => {
    console.log("Add appointment request body:", req.body);

    const { hospitalID, appointmentDate, appointmentTime } = req.body;
    console.log("Parsed values:", hospitalID, appointmentDate, appointmentTime);

    if (!hospitalID || !appointmentDate || !appointmentTime) {
        return res.status(400).json({ error: "Missing required fields" });
    }

    const sql = `
        INSERT INTO appointments (hospitalID, userID, appointmentDate, appointmentTime, status)
        VALUES (?, NULL, ?, ?, 'available')
    `;
    db.query(sql, [hospitalID, appointmentDate, appointmentTime], (err, result) => {
        if (err) {
            console.error("Database insert error:", err);
            return res.status(500).json({ error: "Insert failed" });
        }
        res.json({ message: "Appointment added successfully", id: result.insertId });
    });
});

// Book an appointment
app.post("/api/appointments/book", (req, res) => {
    const { appointmentID, userID } = req.body;

    if (!appointmentID || !userID) {
        return res.status(400).json({ error: "Missing required fields" });
    }
    console.log("Parsed values:", appointmentID, userID);
    const sql = `
        UPDATE appointments
        SET userID = ?, status = 'booked'
        WHERE appointmentID = ? AND status = 'available'
    `;
    db.query(sql, [userID, appointmentID], (err, result) => {
        if (err) return res.status(500).json({ error: "Database update failed" });
        if (result.affectedRows === 0) return res.status(400).json({ error: "Appointment not available" });
        res.json({ message: "Appointment booked successfully!" });
    });
});

// Cancel an appointment
app.post("/api/appointments/cancel", (req, res) => {
    const { appointmentID } = req.body;

    if (!appointmentID) {
        return res.status(400).json({ error: "Missing appointmentID" });
    }

    const sql = `
        UPDATE appointments
        SET userID = NULL, status = 'available'
        WHERE appointmentID = ?
    `;
    db.query(sql, [appointmentID], (err, result) => {
        if (err) return res.status(500).json({ error: "Database update failed" });
        if (result.affectedRows === 0) return res.status(404).json({ error: "Appointment not found" });
        res.json({ message: "Appointment cancelled successfully!" });
    });
});

app.get("/api/appointments/user/:userID", (req, res) => {
    const userID = req.params.userID;

    if (!userID) {
        return res.status(400).json({ error: "Missing userID" });
    }

    const sql = `
        SELECT *
        FROM appointments
        WHERE userID = ?
    `;

    db.query(sql, [userID], (err, results) => {
        if (err) {
            console.error("Database query failed:", err);
            return res.status(500).json({ error: "Database error" });
        }

        if (results.length === 0) {
            return res.json([]);
        }

        res.json(results);
    });
});

// RECORDS

app.get("/api/records/:userID", (req, res) => {
    const { userID } = req.params;

    if (!userID) {
        return res.status(400).json({ error: "Missing userID" });
    }

    const sql = "SELECT * FROM records WHERE userID = ? LIMIT 1";

    db.query(sql, [userID], (err, results) => {
        if (err) {
            console.error("Database query failed:", err);
            return res.status(500).json({ error: "Database error" });
        }

        if (results.length === 0) {
            // No record found for this user
            return res.json(null);
        }

        res.json(results[0]);
    });
});

app.post("/api/records/update", (req, res) => {
    const { userID, allergies, medications, problems } = req.body;

    if (!userID) {
        return res.status(400).json({ error: "Missing userID" });
    }

    const sql = `
        INSERT INTO records (userID, allergies, medications, problems, updatedAt)
        VALUES (?, ?, ?, ?, NOW())
        ON DUPLICATE KEY UPDATE
            allergies = VALUES(allergies),
            medications = VALUES(medications),
            problems = VALUES(problems),
            updatedAt = NOW()
    `;

    db.query(sql, [userID, allergies || "", medications || "", problems || ""], (err, result) => {
        if (err) {
            console.error("Database upsert failed:", err);
            return res.status(500).json({ error: "Database error" });
        }

        res.json({ message: "Record updated successfully!" });
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
