// Create fake process.env object
(window as any).process = { env: {} };

console.log('[IcePush] Loading...');
const baseURL = "../node_modules";
const script = document.createElement("script");
script.src = baseURL + "/systemjs/dist/system.js";
script.async = true;
script.onload = () => {
    SystemJS.config({
        baseURL: baseURL,
        packages: {
            "./js": {
                meta: {
                    "*": {
                        deps: []
                    }
                }
            }
        },
        packageConfigPaths: [
            baseURL + "/@*/*/package.json", // support NPM scoped packages
            baseURL + "/*/package.json", // support global NPM packages
        ]
    });
    console.log("[IcePush] Loading game...");
    SystemJS.import("buffer").then((bufferModule: any) => {
        (window as any).Buffer = bufferModule.Buffer;
    }).then(() => {
        return SystemJS.import('./dist/icepush/IcePush');
    });
};
// Append script to document head
document.head!.appendChild(script);