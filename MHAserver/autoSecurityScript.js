module.exports = function securityAgent(req, res, next) {
    
    const timestamp = new Date().toISOString();
    const ip = req.ip;
    const route = req.originalUrl;
    const method = req.method;

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



    const sqlInjectionPatterns = [
        /\bUNION\b/i,
        /\bSELECT\b/i,
        /\bINSERT\b/i,
        /\bDELETE\b/i,
        /\bDROP\b/i,
        /--/g,      
        /;/g,       
        /['"]/g     
    ];

    let isSuspicious = sqlInjectionPatterns.some(p => p.test(userInput));


    if (isSuspicious) {
        console.log("Suspicious request detected!");
        console.log("Time:", timestamp);
        console.log("IP:", ip);
        console.log("Route:", route);
        console.log("Method:", method);
        console.log("User Input:", userInput);

        return res.status(403).json({
            error: "Security agent blocked suspicious request"
        });
    }

    next();
};
