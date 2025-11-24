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
        /\bunion\s+select\b/,
        /\bdrop\s+table\b/,
        /\bor\s+1=1\b/,
        /['"].*--/,                 // "' anything --"
        /;.*(drop|insert|delete)/   // "; DROP ..."
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
