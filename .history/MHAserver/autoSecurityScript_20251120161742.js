module.exports = function securityAgent(req, res, next) {
    const timestamp = new Date().toISOString();
    const ip = req.ip;
    const route = req.originalUrl;
    const method = req.method;

    // Combine all user input into a String
    const userInput = JSON.stringify({
        body: req.body,
        query: req.query,
        params: req.params
    });

    // Suspicious patterns to check for
    const patterns = [
        /\bUNION\b/i,
        /\bSELECT\b/i,
        /\bINSERT\b/i,
        /\bDELETE\b/i,
        /\bDROP\b/i,
        /--/g,      // SQL comments
        /;/g,       // Statement chaining
        /['"]/g     // unexpected quotes
    ];

    let isSuspicious = false;

    for (const p of patterns) {
        if (p.test(userInput)) {
            isSuspicious = true;
            break;
        }
    }

    if (isSuspicious) {
        console.log("SECURITY ALERT");
        console.log("----------------------------------------------------");
        console.log("Suspicious request detected!");
        console.log("Time:", timestamp);
        console.log("IP:", ip);
        console.log("Route:", route);
        console.log("Method:", method);
        console.log("User Input:", userInput);
        console.log("----------------------------------------------------");

        return res.status(403).json({
            error: "Security agent blocked suspicious request",
            event: "SECURITY_TRIGGERED"
        });
    }

    next();
};