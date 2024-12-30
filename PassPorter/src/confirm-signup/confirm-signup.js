import Alpine from "alpinejs";

export default {
    confirmationKey: '',
    status: 'pending',
    error:'',
    async confirmRegistration(){
        // get the user id from the params store
        let userId = Alpine.store('routeData').params.userId;

        try {
            const response = await fetch(`${process.env.API_URL}/account/confirm`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    confirmationKey: this.confirmationKey,
                    userId
                }),
            });
            if (!response.ok) {
                const error = await response.json();
                console.log("im in error");
                console.log(error);
                this.error = error.message || 'An error occurred';
                this.status = 'error';
                this.confirmationKey ='';
                return;
            }
            this.status = 'confirmed';
        } catch (err) {
            console.log(err);
            this.error = 'Failed to connect to the server. Please try again later.';
        }
    },
    init() {
        window.Router.updatePageLinks();
    }
}