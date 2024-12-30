import Navigo from 'navigo';

/**
 * Create a new Navigo router instance without any routes. The base setup is that all params are
 * stored in the alpine store when a route is resolved.
 * Furthermore it provides a utility function load content dynamically
 * @returns {*}
 */
function createRouter(){
    console.log("Initializing router");

    const router = new Navigo('/', {hash: true});
    // wait for Alpine to be ready
    document.addEventListener('alpine:init', () => {
        const Alpine = window.Alpine;
        console.log("Alpine is ready. Initializing base router");
        Alpine.store('routeData', {
            params: null,
            data: null,
        });

        router.hooks({
            after: (params) => {
                // Store route params and data in the global routeData object
                Alpine.store('routeData').params = params.params; // Parameters from the route, if any
                Alpine.store('routeData').data = params.data; // Other data, if any
                console.log('Route data saved in Alpine store:', Alpine.store('routeData'));
            }
        });
    });
    return router;
}

let router = null;

export function getRouterInstance() {
    if(!router) {
        router = createRouter();
    }
    return router;
}

function updateContent(containerId, htmlText) {
    console.log("update content");
    const container = document.getElementById(containerId);
    try {
        container.innerHTML = htmlText;
    } catch (error) {
        console.error("Error loading HTML with script:", error);
    }
}

// Load HTML content based on path and update specified container
export async function loadContent(containerId, path) {
    try {
        const response = await fetch(`${path}.html`);
        if (!response.ok) throw new Error("Page not found");
        console.log("got page");
        const html = await response.text();
        updateContent(containerId, html);
    } catch (error) {
        updateContent(containerId, "<h2>404 - Page Not Found</h2>");
    }
}


// Call `resolve` to start the router and intercept clicks



