# Buggy

Buggy is a kickstart web application built on a streamlined tech stack. The project has two primary goals:

## Goals

### **1. Keep it Simple**
Avoid large frameworks like Spring on the backend or Angular/Vue/React on the frontend. While these frameworks can bring quick wins at the start of a project, they often introduce complexity and overhead that slows down development in the long run. Buggy is designed to remain transparent and maintainable.

### **2. Start Fast**
Starting a web application often requires essential features like user and account management, authentication, and authorization. These tasks can slow down the initial momentum of your idea. Buggy provides a foundational setup, so you can focus on the core functionality of your idea without losing your flow.

## Tech Stack

### **Frontend**
- **Alpine.js**: Lightweight reactive JavaScript framework.
- **Bulma.css**: Modern CSS framework for styling.

### **Backend**
- **Java**: Core backend language.
- **Palladian**: Simplifies backend tasks.
- **Jersey**: RESTful web service framework.
- **Nopancho ODM**: Object Document Mapping for MongoDB.

### **Database**
- **MongoDB**: Document-based NoSQL database.

## Architecture
Buggy consists of two main applications:

1. **Passporter**: Handles user authentication and management tasks such as signup, login, forgot password, and confirmation code processing.
2. **MainApp**: A sample entry-point application that demonstrates token-based authentication and ensures users are properly authenticated.

## Getting Started

### **Setting up Buggy**
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Buggy
   ```

2. Build the backend:
   ```bash
   mvn clean install
   ```
   This compiles the Java sources and creates the WAR file. Deploy it to a Tomcat server or start the server using Maven:
   ```bash
   mvn tomcat6:run-war
   ```
   By default, the application runs on port `8181`. You can adjust the port in the `pom.xml` file.

3. Build the frontend for **Passporter**:
   ```bash
   cd PassPorter
   npm install
   npm run build
   ```
   The build artifacts are placed in the `dist` folder, ready for hosting via Apache or any other web server. Alternatively, start a development server:
   ```bash
   npm run start
   ```
   This launches a local web server and opens the application in your browser, featuring menu options for login and signup.

### **Running the MainApp**
After successful login, the backend redirects to your target application and sets a JWT as a cookie. You can configure the target application in the `core.properties` file.

To start the bundled example **MainApp**:

1. Navigate to the MainApp directory:
   ```bash
   cd MainApp
   ```

2. Install dependencies and build the project:
   ```bash
   npm install
   npm run build
   ```

3. Start the development server:
   ```bash
   npm run start
   ```

The MainApp includes logic to:
- Check for a token in local storage before every route change.
- Handle token-based authentication.
- Redirect unauthenticated users to **Passporter**.

Feel free to customize **MainApp** as your entry point.

---

## Have Fun!
Buggy is designed to help you launch your web application quickly and effectively. Focus on your idea and let Buggy handle the heavy lifting for user and account management. Happy coding!