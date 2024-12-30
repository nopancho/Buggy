import Alpine from "alpinejs";

export default {
    newPassword: '',
    status: 'pending',
    error: '',
    async resetPassword() {
        console.log("resetting");
        if(!this.validateFields()) {
            console.log("not valid");
            return;
        }
        console.log("valid");
        // get the user id from the params store
        let userId = Alpine.store('routeData').params.userId;
        let forgotPasswordKey = Alpine.store('routeData').params.forgotPasswordKey;

        try {
            const response = await fetch(`${process.env.API_URL}/account/reset`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    forgotPasswordKey: forgotPasswordKey,
                    userId: userId,
                    newPassword: this.newPassword
                }),
            });
            if (!response.ok) {
                const error = await response.json();
                console.log("im in error");
                console.log(error);
                this.error = error.message || 'An error occurred';
                this.status = 'error';
                this.confirmationKey = '';
                return;
            }
            this.status = 'success';
        } catch (err) {
            console.log(err);
            this.error = 'Failed to connect to the server. Please try again later.';
        }
    },
    validateFields() {
        // Simple client-side validation
        if (!this.newPassword || this.newPassword.length < 6) {
            this.error = 'Password must be at least 6 characters long.';
            return false;
        }
        return true;
    },
    init() {
        window.Router.updatePageLinks();
    }
}