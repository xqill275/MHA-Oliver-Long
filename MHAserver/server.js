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

// Basic test route
app.get("/", (req, res) => {
    res.send("MHA API is running");
});

// Example: Fetch all users
app.get("/api/users", (req, res) => {
    db.query("SELECT * FROM users", (err, results) => {
        if (err) {
            console.error(err);
            res.status(500).json({ error: "Database error" });
        } else {
            res.json(results);
        }
    });
});

app.get("/api/user", (req, res) => {
    const userId = req.query.id;

    if (!userId) {
        return res.status(400).json({ error: "Missing user ID in query" });
    }

    db.query("SELECT * FROM users WHERE UID = ?", [userId], (err, results) => {
        if (err) {
            console.error(err);
            return res.status(500).json({ error: "Database query error" });
        }

        if (results.length === 0) {
            return res.status(404).json({ error: "User not found" });
        }

        res.json(results[0]);
    });
});

app.put("/api/user/role", (req, res) => {
    const { uid, role } = req.body;

    if (!uid || !role) {
        return res.status(400).json({ error: "Missing uid or role" });
    }

    const sql = "UPDATE users SET Role = ? WHERE UID = ?";
    db.query(sql, [role, uid], (err, result) => {
        if (err) {
            console.error(err);
            return res.status(500).json({ error: "Database update failed" });
        }

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: "User not found" });
        }

        res.json({ message: "Role updated successfully!" });
    });
});

// Example: Add a new user (simplified)
app.post("/api/users", (req, res) => {
    const { FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash } = req.body;
    const sql = `
        INSERT INTO users (FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;
    db.query(sql, [FullName, Email, PhoneNum, NHSnum, DateOfBirth, Role, EmailHash, NHSHash, DOBHash], (err, result) => {
        if (err) {
            console.error(err);
            res.status(500).json({ error: "Insert failed" });
        } else {
            res.json({ message: "User added successfully", id: result.insertId });
        }
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});