// Helper function to get the token from cookies
function getTokenFromCookies(cookieName = 'token') {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === cookieName) {
            return decodeURIComponent(value);
        }
    }
    return null; // Return null if token is not found
}

// Wrapper function for fetch with Bearer token
async function fetchWithAuth(url, options = {}) {
    const token = getTokenFromCookies(); // Replace 'token' if your cookie name differs

    // Ensure headers exist in the options
    options.headers = {
        ...options.headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    try {
        // Perform the fetch request
        const response = await fetch(url, options);

        // Handle response errors
        if (!response.ok) {
            console.error(`HTTP Error: ${response.status} ${response.statusText}`);
            throw new Error(`Request failed with status ${response.status}`);
        }

        return response; // Return the fetch response
    } catch (error) {
        console.error('Fetch with Auth failed:', error);
        throw error; // Propagate the error
    }
}

export default fetchWithAuth;
