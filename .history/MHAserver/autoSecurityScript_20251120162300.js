module.exports = function securityAgent(req, res, next) {

    // 1. Extract only string inputs from the request
    const inputs = [];

    function extractStrings(obj) {
        if (!obj) return;
        Object.values(obj).forEach(v => {
            if (typeof v === "string") inputs.push(v);
        });
    }

    extractStrings(req.body);
    extractStrings(req.query);
    extractStrings(req.params);

    const userInput = inputs.join(" ").toLowerCase();


    // 2. Realistic SQL injection signatures
    const sqlInjectionPatterns = [
        /\bUNION\b/i,
        /\bSELECT\b/i,
        /\bINSERT\b/i,
        /\bDELETE\b/i,
        /\bDROP\b/i,
        /--/g,      // SQL comments
        /;/g,       // Statement chaining
        /['"]/g     // unexpected quotes
    ];

    let isSuspicious = sqlInjectionPatterns.some(p => p.test(userInput));


    if (isSuspicious) {
        console.log("⚠️ SECURITY AGENT ALERT");
        console.log("Suspicious request blocked:", userInput);

        return res.status(403).json({
            error: "Security agent blocked suspicious request"
        });
    }

    next();
};
