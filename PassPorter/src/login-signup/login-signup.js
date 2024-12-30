import Alpine from "alpinejs";

export default {
    state: 'login', // Default state
    email: '',
    password: '',
    error: Alpine.store('routeData') && Alpine.store('routeData').params && Alpine.store('routeData').params.error
        ? 'Email or password are not correct'
        : '',
    baseUrl: process.env.API_URL,
    async submit() {
        this.error = ''; // Clear any previous error messages

        // Validate the form fields
        if (!this.validateFields()) return;

        // Define the endpoint based on the state
        let endpoint =
            this.state === 'login' ? '/login' :
                this.state === 'signup' ? '/register' :
                    '/forgot-password';

        endpoint = this.baseUrl + 'account'+endpoint; // Prepend the base URL
        // Create the request payload
        const payload = {
            email: this.email,
            ...(this.state !== 'forgotPassword' && { password: this.password }), // Include password only for login/signup
        };
        if(this.state == 'forgotPassword' || this.state == 'signup') {
            try {

                // Send the request using fetch
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload),
                });

                if (!response.ok) {
                    const error = await response.json();
                    this.error = error.message || 'An error occurred';
                    return;
                }

                // Handle success based on the state
                if (this.state === 'signup') {
                    const user = await response.json();
                    console.log(user);
                    // redirect to the key confirmation page
                    window.Router.navigate('/confirm-signup?userId=' + user._id);
                } else if (this.state === 'login') {
                    window.location.href = '/dashboard'; // Redirect to dashboard or other page
                } else if (this.state === 'forgotPassword') {
                    this.setState('login');
                }
            } catch (err) {
                console.log(err);
                this.error = 'Failed to connect to the server. Please try again later.';
            }
        } else if(this.state == 'login') {
            // when login is used we send a plain old form post
            let form = document.createElement('form');
            form.method = 'POST';
            form.action = endpoint;
            form.style.display = 'none';
            let emailField = document.createElement('input');
            emailField.type = 'text';
            emailField.name = 'email';
            emailField.value = this.email;
            form.appendChild(emailField);
            let passwordField = document.createElement('input');
            passwordField.type = 'password';
            passwordField.name = 'password';
            passwordField.value = this.password;
            form.appendChild(passwordField);
            document.body.appendChild(form);
            form.submit();
        }
    },

    validateFields() {
        // Simple client-side validation
        if (!this.email || !this.email.includes('@')) {
            this.error = 'Please enter a valid email address.';
            return false;
        }
        if ((this.state === 'login' || this.state === 'signup') && (!this.password || this.password.length < 6)) {
            this.error = 'Password must be at least 6 characters long.';
            return false;
        }
        return true;
    },

    setState(newState) {
        this.state = newState;
        this.email = '';
        this.password = '';
        this.error = ''; // Reset form fields and errors when state changes
    },
};