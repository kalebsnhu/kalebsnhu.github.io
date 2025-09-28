import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebApplication {
    private static WebServer webServer;
    private static SimpleDataManager dataManager;
    private static AuthenticationSystem authSystem;
    private static AnimalService animalService;
    private static MonitoringSystem monitoringSystem;
    private static ReservationService reservationService;
    
    /*
    Function: main
    @params:
    args: command line arguments
    Description: Application entry point that initializes and starts the web server
    */
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Starting Rescue Animal Web Application");
        System.out.println("Enhanced with Role-Based User Management");
        System.out.println("=".repeat(60));
        
        if (!initializeSystems()) {
            System.err.println("Failed to initialize systems. Exiting.");
            return;
        }
        
        if (!setupWebEnvironment()) {
            System.err.println("Failed to setup web environment. Exiting.");
            return;
        }
        
        addSampleDataIfEmpty();
        createSampleUsersIfNeeded();
        setupShutdownHook();
        
        int port = 8080;
        webServer = new WebServer(port, authSystem, animalService, 
                                monitoringSystem, reservationService, dataManager);
        
        displayStartupInformation(port);
        webServer.start();
    }
    
    /*
    Function: initializeSystems
    @params: none
    Description: Initializes all system components and services
    */
    private static boolean initializeSystems() {
        try {
            System.out.println("Initializing data manager...");
            dataManager = new SimpleDataManager();
            
            System.out.println("Initializing authentication system...");
            authSystem = new AuthenticationSystem(dataManager);
            
            System.out.println("Initializing monitoring system...");
            monitoringSystem = new MonitoringSystem(dataManager);
            
            System.out.println("Initializing animal service...");
            animalService = new AnimalService(monitoringSystem, dataManager);
            
            System.out.println("Initializing reservation service...");
            reservationService = new ReservationService(animalService, monitoringSystem);
            
            System.out.println("All systems initialized successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error initializing systems: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /*
    Function: createSampleUsersIfNeeded
    @params: none
    Description: Creates sample users with different roles if database is empty
    */
    private static void createSampleUsersIfNeeded() {
        try {
            if (dataManager.getUsers().size() <= 1) {
                System.out.println("Creating sample users with different roles...");
                
                boolean staffCreated = authSystem.createUser(
                    "staff_user", 
                    "staff123", 
                    "Sarah Staff", 
                    UserRole.STAFF
                );
                
                boolean monitorCreated = authSystem.createUser(
                    "monitor_user", 
                    "monitor123", 
                    "Mike Monitor", 
                    UserRole.MONITOR
                );
                
                boolean viewCreated = authSystem.createUser(
                    "view_user", 
                    "view123", 
                    "Vera Viewer", 
                    UserRole.VIEW
                );
                
                if (staffCreated && monitorCreated && viewCreated) {
                    System.out.println("Sample users created successfully");
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating sample users: " + e.getMessage());
        }
    }
    
    /*
    Function: addSampleDataIfEmpty
    @params: none
    Description: Adds sample animals if database is empty
    */
    private static void addSampleDataIfEmpty() {
        try {
            if (animalService.getTotalAnimals() == 0) {
                System.out.println("Adding sample animals...");
                
                Dog charlie = new Dog("Charlie", "German Shepherd", "male", "1", "110", 
                                    "2024-01-15", "United States", "intake", false, "United States");
                animalService.addDog(charlie, "system");
                monitoringSystem.updateAnimalLocation("Charlie", "Training Facility A", "system");
                
                Dog popcorn = new Dog("Popcorn", "Lab", "male", "3", "55",
                                    "2023-06-10", "United States", "in service", true, "United States");
                animalService.addDog(popcorn, "system");
                monitoringSystem.updateAnimalLocation("Popcorn", "Service Location - Downtown", "system");
                
                Monkey george = new Monkey("George", "male", "5", "75", "Marmoset", "3", "5", "8",
                                         "2023-03-20", "Brazil", "in service", false, "United States");
                animalService.addMonkey(george, "system");
                monitoringSystem.updateAnimalLocation("George", "Training Facility C", "system");
                
                Cat whiskers = new Cat("Whiskers", "Persian", "Gray", false, "female", "2", "8",
                                     "2024-02-01", "United States", "in service", false, "United States");
                animalService.addCat(whiskers, "system");
                
                Cat luna = new Cat("Luna", "Siamese", "Cream", false, "female", "4", "6",
                                 "2023-08-15", "United States", "Phase II", false, "United States");
                animalService.addCat(luna, "system");
                
                Bird sunny = new Bird("Sunny", "Cockatiel", "12 inches", true, "Curved", "male", "3", "0.2",
                                    "2023-11-05", "Australia", "in service", false, "United States");
                animalService.addBird(sunny, "system");
                
                Bird echo = new Bird("Echo", "African Grey Parrot", "18 inches", true, "Large Curved", 
                                   "female", "6", "0.5", "2023-05-12", "Congo", "Phase I", false, "United States");
                animalService.addBird(echo, "system");
                
                Rabbit clover = new Rabbit("Clover", "Holland Lop", "Brown", "lopped", true, "female", "2", "3",
                                         "2024-01-20", "United States", "in service", false, "United States");
                animalService.addRabbit(clover, "system");
                
                Rabbit pepper = new Rabbit("Pepper", "Mini Rex", "Black", "upright", true, "male", "1", "2.5",
                                         "2024-03-10", "United States", "intake", false, "United States");
                animalService.addRabbit(pepper, "system");
                
                System.out.println("Sample animals added successfully");
            }
        } catch (Exception e) {
            System.err.println("Error adding sample data: " + e.getMessage());
        }
    }
    
    /*
    Function: setupWebEnvironment
    @params: none
    Description: Creates web directories and HTML files
    */
    private static boolean setupWebEnvironment() {
        try {
            System.out.println("Setting up web environment...");
            
            createDirectory("web");
            createDirectory("web/css");
            createDirectory("web/js");
            createDirectory("web/images");
            createDirectory("data");
            
            createDynamicHtmlFiles();
            
            System.out.println("Web environment setup complete");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error setting up web environment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /*
    Function: createDirectory
    @params:
    path: directory path to create
    Description: Creates a directory if it doesn't exist
    */
    private static void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Created directory: " + path);
            } else {
                System.err.println("Failed to create directory: " + path);
            }
        }
    }
    
    /*
    Function: createDynamicHtmlFiles
    @params: none
    Description: Creates the HTML files for the web interface
    */
    private static void createDynamicHtmlFiles() throws IOException {
        String indexHtml = getLoginPageHtml();
        Files.write(Paths.get("web/index.html"), indexHtml.getBytes());
        System.out.println("Created dynamic index.html");
        
        String dashboardHtml = getEnhancedDashboardHtml();
        Files.write(Paths.get("web/dashboard.html"), dashboardHtml.getBytes());
        System.out.println("Created enhanced dashboard.html");
    }
    
    /*
    Function: getLoginPageHtml
    @params: none
    Description: Returns the HTML content for the login page with fixed form toggle
    */
    private static String getLoginPageHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rescue Animal Management System</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #1e3a5f 0%, #2c5f8c 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; color: #333; }
        .login-container { background: rgba(255, 255, 255, 0.95); border-radius: 20px; padding: 40px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3); width: 100%; max-width: 450px; backdrop-filter: blur(10px); }
        .logo { text-align: center; margin-bottom: 30px; }
        .logo h1 { color: #1e3a5f; font-size: 2.2em; font-weight: 700; margin-bottom: 10px; }
        .logo p { color: #666; font-size: 1.1em; }
        .alert { padding: 12px 16px; border-radius: 10px; margin-bottom: 20px; font-weight: 500; display: none; }
        .alert.error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .alert.success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #1e3a5f; font-weight: 600; }
        .form-group input { width: 100%; padding: 14px 16px; border: 2px solid #e1e5e9; border-radius: 10px; font-size: 16px; transition: all 0.3s ease; }
        .form-group input:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1); }
        .btn { width: 100%; padding: 14px; border: none; border-radius: 10px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; }
        .btn-primary { background: linear-gradient(135deg, #007bff, #0056b3); color: white; }
        .btn-primary:hover { background: linear-gradient(135deg, #0056b3, #004085); transform: translateY(-2px); }
        .btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
        .form-footer { text-align: center; margin-top: 25px; padding-top: 20px; border-top: 1px solid #e1e5e9; }
        .btn-link { color: #007bff; text-decoration: none; font-weight: 600; padding: 8px 16px; border-radius: 6px; transition: all 0.3s ease; }
        .btn-link:hover { background: rgba(0, 123, 255, 0.1); }
        .demo-accounts { background: #f8f9fa; border-radius: 10px; padding: 20px; margin-top: 20px; border-left: 4px solid #007bff; }
        .demo-accounts h4 { color: #1e3a5f; margin-bottom: 12px; }
        .demo-accounts .account { background: white; padding: 10px 12px; border-radius: 6px; margin-bottom: 8px; border: 1px solid #e1e5e9; font-family: monospace; cursor: pointer; transition: all 0.3s ease; }
        .demo-accounts .account:hover { background: #f8f9fa; border-color: #007bff; }
        .register-form { display: none; }
        .register-form.active { display: block; }
        .login-form.hidden { display: none; }
        .loading { display: inline-block; margin-left: 10px; }
        .loading::after { content: ''; display: inline-block; width: 16px; height: 16px; border: 2px solid #ffffff; border-radius: 50%; border-top-color: transparent; animation: spin 0.8s linear infinite; }
        @keyframes spin { to { transform: rotate(360deg); } }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="logo">
            <h1>Rescue Animal System</h1>
            <p>Management Dashboard</p>
        </div>
        <div id="alertContainer"></div>
        
        <form id="loginForm" class="login-form" onsubmit="handleLogin(event)">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" required autocomplete="username">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required autocomplete="current-password">
            </div>
            <button type="submit" class="btn btn-primary" id="loginBtn">
                Sign In
                <span id="loginLoading" class="loading" style="display: none;"></span>
            </button>
            <div class="form-footer">
                <p>Don't have an account?</p>
                <a href="#" class="btn-link" onclick="showRegisterForm()">Create Account</a>
            </div>
        </form>

        <form id="registerForm" class="register-form" onsubmit="handleRegister(event)">
            <div class="form-group">
                <label for="regUsername">Username</label>
                <input type="text" id="regUsername" name="username" required autocomplete="username">
            </div>
            <div class="form-group">
                <label for="regFullName">Full Name</label>
                <input type="text" id="regFullName" name="fullName" required autocomplete="name">
            </div>
            <div class="form-group">
                <label for="regPassword">Password</label>
                <input type="password" id="regPassword" name="password" required minlength="6" autocomplete="new-password">
            </div>
            <button type="submit" class="btn btn-primary" id="registerBtn">
                Create Account
                <span id="registerLoading" class="loading" style="display: none;"></span>
            </button>
            <div class="form-footer">
                <p>Already have an account?</p>
                <a href="#" class="btn-link" onclick="showLoginForm()">Sign In</a>
            </div>
        </form>

        <div class="demo-accounts">
            <h4>Demo Account</h4>
            <div class="account" onclick="quickLogin('admin', 'admin123')">Admin: admin / admin123</div>
            <p style="color: #666; font-size: 0.9em; margin-top: 10px;">Regular users can view animals but cannot add/delete them.</p>
        </div>
    </div>

    <script>
        /*
        Function: handleLogin
        @params:
        event: form submit event
        Description: Handles user login form submission
        */
        function handleLogin(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const username = formData.get('username');
            const password = formData.get('password');

            if (!username || !password) {
                showAlert('Please enter both username and password', 'error');
                return;
            }

            setLoading('login', true);

            fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                setLoading('login', false);
                if (data.success) {
                    showAlert('Login successful! Redirecting...', 'success');
                    setTimeout(() => window.location.href = '/dashboard.html', 1000);
                } else {
                    showAlert(data.message || 'Invalid username or password', 'error');
                }
            })
            .catch(error => {
                setLoading('login', false);
                showAlert('Login failed. Please check your connection and try again.', 'error');
            });
        }

        /*
        Function: handleRegister
        @params:
        event: form submit event
        Description: Handles user registration form submission
        */
        function handleRegister(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const username = formData.get('username');
            const fullName = formData.get('fullName');
            const password = formData.get('password');

            if (!username || !fullName || !password) {
                showAlert('Please fill in all fields', 'error');
                return;
            }

            setLoading('register', true);

            fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `username=${encodeURIComponent(username)}&fullName=${encodeURIComponent(fullName)}&password=${encodeURIComponent(password)}`,
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                setLoading('register', false);
                if (data.success) {
                    showAlert('Account created successfully! Please sign in.', 'success');
                    document.getElementById('registerForm').reset();
                    showLoginForm();
                } else {
                    showAlert(data.message || 'Registration failed. Username may already exist.', 'error');
                }
            })
            .catch(error => {
                setLoading('register', false);
                showAlert('Registration failed. Please check your connection and try again.', 'error');
            });
        }

        /*
        Function: showLoginForm
        @params: none
        Description: Shows login form and hides registration form
        */
        function showLoginForm() {
            const loginForm = document.getElementById('loginForm');
            const registerForm = document.getElementById('registerForm');
            
            loginForm.classList.remove('hidden');
            loginForm.style.display = 'block';
            
            registerForm.classList.remove('active');
            registerForm.style.display = 'none';
            
            clearAlert();
        }

        /*
        Function: showRegisterForm
        @params: none
        Description: Shows registration form and hides login form
        */
        function showRegisterForm() {
            const loginForm = document.getElementById('loginForm');
            const registerForm = document.getElementById('registerForm');
            
            loginForm.classList.add('hidden');
            loginForm.style.display = 'none';
            
            registerForm.classList.add('active');
            registerForm.style.display = 'block';
            
            clearAlert();
        }

        /*
        Function: showAlert
        @params:
        message: alert message text
        type: alert type (success/error)
        Description: Displays alert message to user
        */
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alertContainer');
            alertContainer.innerHTML = `<div class="alert ${type}" style="display: block;">${message}</div>`;
            if (type === 'success') {
                setTimeout(() => clearAlert(), 3000);
            }
        }

        /*
        Function: clearAlert
        @params: none
        Description: Clears any displayed alert messages
        */
        function clearAlert() {
            document.getElementById('alertContainer').innerHTML = '';
        }

        /*
        Function: quickLogin
        @params:
        username: username to fill
        password: password to fill
        Description: Fills login form with provided credentials
        */
        function quickLogin(username, password) {
            document.getElementById('username').value = username;
            document.getElementById('password').value = password;
            showLoginForm();
        }

        /*
        Function: setLoading
        @params:
        type: button type (login/register)
        loading: loading state boolean
        Description: Shows or hides loading spinner on buttons
        */
        function setLoading(type, loading) {
            const btn = document.getElementById(type + 'Btn');
            const loadingSpan = document.getElementById(type + 'Loading');
            btn.disabled = loading;
            loadingSpan.style.display = loading ? 'inline-block' : 'none';
        }

        /*
        Function: window.onload
        @params: none
        Description: Initializes page on load and checks authentication
        */
        window.addEventListener('load', function() {
            showLoginForm();
            
            fetch('/api/user', { credentials: 'include' })
            .then(response => {
                if (response.ok) {
                    window.location.href = '/dashboard.html';
                }
            })
            .catch(() => {});
        });
    </script>
</body>
</html>
""";
    }
    
    /*
    Function: getEnhancedDashboardHtml
    @params: none
    Description: Returns the HTML content for the dashboard page with fixed edit functionality
    */
    private static String getEnhancedDashboardHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Rescue Animal Management System</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; color: #333; }
        .navbar { background: linear-gradient(135deg, #1e3a5f, #2c5f8c); color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .navbar-brand { font-size: 1.5rem; font-weight: bold; }
        .navbar-user { display: flex; align-items: center; gap: 15px; }
        .user-info { text-align: right; }
        .logout-btn { background: rgba(255,255,255,0.2); color: white; border: 1px solid rgba(255,255,255,0.3); padding: 8px 16px; border-radius: 6px; cursor: pointer; transition: all 0.3s ease; }
        .logout-btn:hover { background: rgba(255,255,255,0.3); }
        .container { max-width: 1400px; margin: 0 auto; padding: 2rem; }
        .dashboard-title { font-size: 2.5rem; color: #1e3a5f; margin-bottom: 0.5rem; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
        .stat-card { background: white; border-radius: 15px; padding: 1.5rem; box-shadow: 0 4px 15px rgba(0,0,0,0.1); transition: transform 0.3s ease; border-left: 4px solid; }
        .stat-card:hover { transform: translateY(-5px); }
        .stat-card.total { border-left-color: #007bff; }
        .stat-card.available { border-left-color: #28a745; }
        .stat-card.reserved { border-left-color: #dc3545; }
        .stat-card.training { border-left-color: #ffc107; }
        .stat-number { font-size: 2.5rem; font-weight: bold; margin-bottom: 0.5rem; }
        .stat-card.total .stat-number { color: #007bff; }
        .stat-card.available .stat-number { color: #28a745; }
        .stat-card.reserved .stat-number { color: #dc3545; }
        .stat-card.training .stat-number { color: #ffc107; }
        .stat-label { color: #666; font-size: 1.1rem; font-weight: 500; }
        .action-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
        .action-card { background: white; border-radius: 15px; padding: 1.5rem; box-shadow: 0 4px 15px rgba(0,0,0,0.1); transition: all 0.3s ease; text-align: center; cursor: pointer; border: none; }
        .action-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }
        .action-card:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
        .action-icon { font-size: 2.5rem; margin-bottom: 1rem; }
        .action-title { font-size: 1.3rem; font-weight: 600; margin-bottom: 0.5rem; }
        .action-description { color: #666; font-size: 0.95rem; }
        .role-restricted { display: none; }
        .alert { padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border: 1px solid; }
        .alert.error { background: #f8d7da; color: #721c24; border-color: #f5c6cb; }
        .alert.success { background: #d4edda; color: #155724; border-color: #c3e6cb; }
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); }
        .modal-content { background-color: white; margin: 5% auto; padding: 30px; border-radius: 15px; width: 90%; max-width: 800px; max-height: 80vh; overflow-y: auto; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
        .modal-title { font-size: 1.5rem; color: #1e3a5f; margin: 0; }
        .close { color: #999; font-size: 28px; font-weight: bold; cursor: pointer; transition: color 0.3s; }
        .close:hover { color: #333; }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 15px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: 600; color: #333; }
        .form-group input, .form-group select { width: 100%; padding: 12px; border: 2px solid #e1e5e9; border-radius: 8px; font-size: 14px; transition: border-color 0.3s; }
        .form-group input:focus, .form-group select:focus { outline: none; border-color: #007bff; }
        .btn { padding: 12px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; margin-right: 10px; }
        .btn-primary { background: #007bff; color: white; }
        .btn-primary:hover { background: #0056b3; }
        .btn-secondary { background: #6c757d; color: white; }
        .btn-secondary:hover { background: #545b62; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-danger:hover { background: #c82333; }
        .btn-warning { background: #ffc107; color: #212529; }
        .btn-warning:hover { background: #e0a800; }
        .btn-success { background: #28a745; color: white; }
        .btn-success:hover { background: #1e7e34; }
        .animals-list { max-height: 400px; overflow-y: auto; border: 1px solid #eee; border-radius: 8px; padding: 15px; }
        .animal-item, .user-item { display: flex; justify-content: space-between; align-items: center; padding: 15px; border-bottom: 1px solid #eee; transition: background-color 0.3s; }
        .animal-item:hover, .user-item:hover { background-color: #f8f9fa; }
        .animal-item:last-child, .user-item:last-child { border-bottom: none; }
        .animal-display, .user-display { display: flex; align-items: center; gap: 15px; flex: 1; }
        .animal-image { width: 60px; height: 60px; border-radius: 8px; overflow: hidden; flex-shrink: 0; }
        .animal-image img { width: 100%; height: 100%; object-fit: cover; }
        .animal-placeholder, .user-avatar { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 24px; }
        .dog-placeholder { background: #007bff; }
        .monkey-placeholder { background: #fd7e14; }
        .cat-placeholder { background: #6f42c1; }
        .bird-placeholder { background: #20c997; }
        .rabbit-placeholder { background: #e83e8c; }
        .user-avatar { background: #6c757d; font-size: 20px; }
        .animal-info, .user-info-detail { flex: 1; }
        .animal-info h4, .user-info-detail h4 { margin: 0 0 5px 0; color: #1e3a5f; }
        .animal-info p, .user-info-detail p { margin: 0; color: #666; font-size: 0.9em; }
        .sort-controls { margin-bottom: 15px; padding: 10px; background: #f8f9fa; border-radius: 8px; }
        .sort-controls label { margin-right: 10px; font-weight: 600; }
        .sort-controls select { padding: 5px 10px; border: 1px solid #ddd; border-radius: 4px; margin-right: 15px; }
        .loading { text-align: center; padding: 20px; color: #666; }
        .user-actions { display: flex; gap: 10px; }
        .role-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; }
        .role-admin { background: #dc3545; color: white; }
        .role-staff { background: #ffc107; color: #212529; }
        .role-monitor { background: #17a2b8; color: white; }
        .role-view { background: #6c757d; color: white; }
        .status-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; margin-left: 8px; }
        .status-active { background: #28a745; color: white; }
        .status-inactive { background: #dc3545; color: white; }
        .users-list { max-height: 500px; overflow-y: auto; border: 1px solid #eee; border-radius: 8px; padding: 15px; }
        .tabs { display: flex; border-bottom: 2px solid #e1e5e9; margin-bottom: 20px; }
        .tab { padding: 10px 20px; cursor: pointer; border-bottom: 3px solid transparent; transition: all 0.3s ease; }
        .tab.active { color: #007bff; border-bottom-color: #007bff; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="navbar-brand">Rescue Animal System</div>
        <div class="navbar-user">
            <div class="user-info">
                <div class="user-name" id="userName">Loading...</div>
                <div class="user-role" id="userRole">Loading...</div>
            </div>
            <button onclick="logout()" class="logout-btn">Logout</button>
        </div>
    </nav>

    <div class="container">
        <div class="dashboard-header">
            <h1 class="dashboard-title">Dashboard</h1>
        </div>

        <div id="alertContainer"></div>

        <div class="stats-grid">
            <div class="stat-card total">
                <div class="stat-number" id="totalAnimals">0</div>
                <div class="stat-label">Total Animals</div>
            </div>
            <div class="stat-card available">
                <div class="stat-number" id="availableAnimals">0</div>
                <div class="stat-label">Available</div>
            </div>
            <div class="stat-card reserved">
                <div class="stat-number" id="reservedAnimals">0</div>
                <div class="stat-label">Reserved</div>
            </div>
            <div class="stat-card training">
                <div class="stat-number" id="trainingAnimals">0</div>
                <div class="stat-label">In Training</div>
            </div>
        </div>

        <div class="action-grid">
            <div class="action-card" onclick="viewAllAnimals()">
                <div class="action-icon">*</div>
                <div class="action-title">View All Animals</div>
                <div class="action-description">Browse complete animal registry</div>
            </div>
            <div class="action-card" onclick="viewAvailableAnimals()">
                <div class="action-icon">!</div>
                <div class="action-title">Available Animals</div>
                <div class="action-description">Animals ready for service</div>
            </div>
            <div class="action-card role-restricted" data-min-role="STAFF" onclick="showAddAnimalModal()">
                <div class="action-icon">+</div>
                <div class="action-title">Add Animal</div>
                <div class="action-description">Register new rescue animal</div>
            </div>
            <div class="action-card role-restricted" data-min-role="ADMIN" onclick="showManageAnimalsModal()">
                <div class="action-icon">=</div>
                <div class="action-title">Manage Animals</div>
                <div class="action-description">Edit or delete animals</div>
            </div>
            <div class="action-card role-restricted" data-min-role="STAFF" onclick="showReserveModal()">
                <div class="action-icon">#</div>
                <div class="action-title">Make Reservation</div>
                <div class="action-description">Reserve animal for service</div>
            </div>
            <div class="action-card role-restricted" data-min-role="MONITOR" onclick="viewActivities()">
                <div class="action-icon">@</div>
                <div class="action-title">Activity Log</div>
                <div class="action-description">Monitor system activities</div>
            </div>
            <div class="action-card role-restricted" data-min-role="ADMIN" onclick="showUserManagementModal()">
                <div class="action-icon">&</div>
                <div class="action-title">User Management</div>
                <div class="action-description">Manage users and permissions</div>
            </div>
            <div class="action-card role-restricted" data-min-role="ADMIN" onclick="viewSessions()">
                <div class="action-icon">%</div>
                <div class="action-title">Active Sessions</div>
                <div class="action-description">Monitor user sessions</div>
            </div>
        </div>

    <!-- Add Animal Modal -->
    <div id="addAnimalModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Add New Animal</h2>
                <span class="close" onclick="closeModal('addAnimalModal')">&times;</span>
            </div>
            <form id="addAnimalForm" onsubmit="handleAddAnimal(event)">
                <div class="form-row">
                    <div class="form-group">
                        <label for="animalType">Animal Type</label>
                        <select id="animalType" name="type" required onchange="updateAnimalFields()">
                            <option value="">Select Type</option>
                            <option value="dog">Dog</option>
                            <option value="monkey">Monkey</option>
                            <option value="cat">Cat</option>
                            <option value="bird">Bird</option>
                            <option value="rabbit">Rabbit</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="animalName">Name</label>
                        <input type="text" id="animalName" name="name" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="animalGender">Gender</label>
                        <select id="animalGender" name="gender" required>
                            <option value="">Select Gender</option>
                            <option value="male">Male</option>
                            <option value="female">Female</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="animalAge">Age (years)</label>
                        <input type="number" id="animalAge" name="age" required min="0" max="30">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="animalWeight">Weight</label>
                        <input type="text" id="animalWeight" name="weight" required>
                    </div>
                    <div class="form-group">
                        <label for="acquisitionDate">Acquisition Date</label>
                        <input type="date" id="acquisitionDate" name="acquisitionDate" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="acquisitionCountry">Acquisition Country</label>
                        <input type="text" id="acquisitionCountry" name="acquisitionCountry" required>
                    </div>
                    <div class="form-group">
                        <label for="inServiceCountry">Service Country</label>
                        <input type="text" id="inServiceCountry" name="inServiceCountry" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="trainingStatus">Training Status</label>
                        <select id="trainingStatus" name="trainingStatus" required>
                            <option value="intake">Intake</option>
                            <option value="Phase I">Phase I</option>
                            <option value="Phase II">Phase II</option>
                            <option value="in service">In Service</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="reserved">Reserved</label>
                        <select id="reserved" name="reserved">
                            <option value="false">No</option>
                            <option value="true">Yes</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label for="animalLocation">Current Location</label>
                    <input type="text" id="animalLocation" name="location" placeholder="e.g., Training Facility A" required>
                </div>
                <div id="typeSpecificFields"></div>
                <div style="margin-top: 20px; text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('addAnimalModal')">Cancel</button>
                    <button type="submit" class="btn btn-primary">Add Animal</button>
                </div>
            </form>
        </div>
    </div>

    <!-- View Animals Modal -->
    <div id="viewAnimalsModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title" id="viewAnimalsTitle">All Animals</h2>
                <span class="close" onclick="closeModal('viewAnimalsModal')">&times;</span>
            </div>
            <div class="sort-controls">
                <label>Sort by:</label>
                <select id="sortBy" onchange="applySorting()">
                    <option value="name">Name</option>
                    <option value="type">Type</option>
                    <option value="age">Age</option>
                    <option value="status">Status</option>
                </select>
                <label>Order:</label>
                <select id="sortOrder" onchange="applySorting()">
                    <option value="asc">Ascending</option>
                    <option value="desc">Descending</option>
                </select>
                <label>Filter by Type:</label>
                <select id="typeFilter" onchange="applySorting()">
                    <option value="">All Types</option>
                    <option value="Dog">Dogs</option>
                    <option value="Monkey">Monkeys</option>
                    <option value="Cat">Cats</option>
                    <option value="Bird">Birds</option>
                    <option value="Rabbit">Rabbits</option>
                </select>
            </div>
            <div id="animalsListContainer" class="animals-list">
                <div class="loading">Loading animals...</div>
            </div>
        </div>
    </div>

    <!-- Manage Animals Modal -->
    <div id="manageAnimalsModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Manage Animals</h2>
                <span class="close" onclick="closeModal('manageAnimalsModal')">&times;</span>
            </div>
            <div class="sort-controls">
                <label>Sort by:</label>
                <select id="manageSortBy" onchange="applyManagementSorting()">
                    <option value="name">Name</option>
                    <option value="type">Type</option>
                    <option value="age">Age</option>
                    <option value="status">Status</option>
                </select>
                <label>Order:</label>
                <select id="manageSortOrder" onchange="applyManagementSorting()">
                    <option value="asc">Ascending</option>
                    <option value="desc">Descending</option>
                </select>
                <label>Filter by Type:</label>
                <select id="manageTypeFilter" onchange="applyManagementSorting()">
                    <option value="">All Types</option>
                    <option value="Dog">Dogs</option>
                    <option value="Monkey">Monkeys</option>
                    <option value="Cat">Cats</option>
                    <option value="Bird">Birds</option>
                    <option value="Rabbit">Rabbits</option>
                </select>
            </div>
            <div id="manageAnimalsContainer" class="animals-list">
                <div class="loading">Loading animals...</div>
            </div>
        </div>
    </div>

    <!-- Reserve Animal Modal -->
    <div id="reserveModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Reserve Animal</h2>
                <span class="close" onclick="closeModal('reserveModal')">&times;</span>
            </div>
            <form id="reserveForm" onsubmit="handleReservation(event)">
                <div class="form-group">
                    <label for="reserveAnimalType">Animal Type</label>
                    <select id="reserveAnimalType" name="animalType" required>
                        <option value="">Select Type</option>
                        <option value="dog">Dog</option>
                        <option value="monkey">Monkey</option>
                        <option value="cat">Cat</option>
                        <option value="bird">Bird</option>
                        <option value="rabbit">Rabbit</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="serviceCountry">Service Country</label>
                    <input type="text" id="serviceCountry" name="serviceCountry" required>
                </div>
                <div style="margin-top: 20px; text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('reserveModal')">Cancel</button>
                    <button type="submit" class="btn btn-primary">Reserve Animal</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Animal Modal -->
    <div id="editAnimalModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Edit Animal</h2>
                <span class="close" onclick="closeModal('editAnimalModal')">&times;</span>
            </div>
            <form id="editAnimalForm" onsubmit="handleEditAnimal(event)">
                <input type="hidden" id="editAnimalOriginalName" name="originalName">
                <div class="form-row">
                    <div class="form-group">
                        <label for="editAnimalName">Name</label>
                        <input type="text" id="editAnimalName" name="name" required>
                    </div>
                    <div class="form-group">
                        <label for="editAnimalGender">Gender</label>
                        <select id="editAnimalGender" name="gender" required>
                            <option value="male">Male</option>
                            <option value="female">Female</option>
                        </select>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="editAnimalAge">Age (years)</label>
                        <input type="number" id="editAnimalAge" name="age" required min="0" max="30">
                    </div>
                    <div class="form-group">
                        <label for="editAnimalWeight">Weight</label>
                        <input type="text" id="editAnimalWeight" name="weight" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="editTrainingStatus">Training Status</label>
                        <select id="editTrainingStatus" name="trainingStatus" required>
                            <option value="intake">Intake</option>
                            <option value="Phase I">Phase I</option>
                            <option value="Phase II">Phase II</option>
                            <option value="in service">In Service</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="editReserved">Reserved</label>
                        <select id="editReserved" name="reserved">
                            <option value="false">No</option>
                            <option value="true">Yes</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label for="editAnimalLocation">Current Location</label>
                    <input type="text" id="editAnimalLocation" name="location" required>
                </div>
                <div style="margin-top: 20px; text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('editAnimalModal')">Cancel</button>
                    <button type="submit" class="btn btn-primary">Update Animal</button>
                </div>
            </form>
        </div>
    </div>

    <!-- User Management Modal -->
    <div id="userManagementModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">User Management</h2>
                <span class="close" onclick="closeModal('userManagementModal')">&times;</span>
            </div>
            <div class="tabs">
                <div class="tab active" onclick="switchTab('usersList')">All Users</div>
                <div class="tab" onclick="switchTab('createUser')">Create User</div>
            </div>
            
            <div id="usersList" class="tab-content active">
                <div class="users-list" id="usersListContainer">
                    <div class="loading">Loading users...</div>
                </div>
            </div>
            
            <div id="createUser" class="tab-content">
                <form id="createUserForm" onsubmit="handleCreateUser(event)">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="newUsername">Username</label>
                            <input type="text" id="newUsername" name="username" required>
                        </div>
                        <div class="form-group">
                            <label for="newFullName">Full Name</label>
                            <input type="text" id="newFullName" name="fullName" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="newPassword">Password</label>
                            <input type="password" id="newPassword" name="password" required minlength="6">
                        </div>
                        <div class="form-group">
                            <label for="newRole">Role</label>
                            <select id="newRole" name="role" required>
                                <option value="VIEW">View Only</option>
                                <option value="MONITOR">Monitor</option>
                                <option value="STAFF">Staff</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>
                    </div>
                    <div style="text-align: right;">
                        <button type="submit" class="btn btn-primary">Create User</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Edit User Modal -->
    <div id="editUserModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Edit User</h2>
                <span class="close" onclick="closeModal('editUserModal')">&times;</span>
            </div>
            <form id="editUserForm" onsubmit="handleEditUser(event)">
                <input type="hidden" id="editUsername" name="username">
                <div class="form-group">
                    <label for="editFullName">Full Name</label>
                    <input type="text" id="editFullName" name="fullName" required>
                </div>
                <div class="form-group">
                    <label for="editRole">Role</label>
                    <select id="editRole" name="role" required>
                        <option value="VIEW">View Only</option>
                        <option value="MONITOR">Monitor</option>
                        <option value="STAFF">Staff</option>
                        <option value="ADMIN">Admin</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="newUserPassword">New Password (optional)</label>
                    <input type="password" id="newUserPassword" name="newPassword" minlength="6">
                    <small style="color: #666;">Leave blank to keep current password</small>
                </div>
                <div style="margin-top: 20px; text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('editUserModal')">Cancel</button>
                    <button type="submit" class="btn btn-primary">Update User</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        let currentUser = null;
        let allAnimalsData = null;
        let allUsersData = null;

        const roleHierarchy = {
            'VIEW': 0,
            'MONITOR': 1,
            'STAFF': 2,
            'ADMIN': 3
        };

        /*
        Function: window.onload
        @params: none
        Description: Initializes the page on load
        */
        window.addEventListener('load', function() {
            checkAuthentication();
            loadDashboardData();
        });

        /*
        Function: checkAuthentication
        @params: none
        Description: Checks if user is authenticated and updates UI accordingly
        */
        function checkAuthentication() {
            fetch('/api/user', { credentials: 'include' })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    window.location.href = '/index.html';
                    throw new Error('Not authenticated');
                }
            })
            .then(user => {
                currentUser = user;
                document.getElementById('userName').textContent = user.fullName;
                document.getElementById('userRole').textContent = user.role;
                
                updateUIForRole(user.role);
            })
            .catch(error => {
                console.error('Authentication check failed:', error);
            });
        }

        /*
        Function: updateUIForRole
        @params:
        userRole: user's role string
        Description: Shows/hides UI elements based on user role permissions
        */
        function updateUIForRole(userRole) {
            const userRoleLevel = roleHierarchy[userRole] || 0;
            const restrictedElements = document.querySelectorAll('.role-restricted');
            
            restrictedElements.forEach(element => {
                const minRole = element.getAttribute('data-min-role');
                const minRoleLevel = roleHierarchy[minRole] || 999;
                
                if (userRoleLevel >= minRoleLevel) {
                    element.style.display = 'block';
                } else {
                    element.style.display = 'none';
                }
            });
        }

        /*
        Function: loadDashboardData
        @params: none
        Description: Loads animal statistics and data for the dashboard
        */
        function loadDashboardData() {
            fetch('/api/animals', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                allAnimalsData = data;
                if (data.stats) {
                    document.getElementById('totalAnimals').textContent = data.stats.total;
                    document.getElementById('availableAnimals').textContent = data.stats.available;
                    document.getElementById('reservedAnimals').textContent = data.stats.reserved;
                    document.getElementById('trainingAnimals').textContent = data.stats.training;
                }
            })
            .catch(error => {
                console.error('Failed to load dashboard data:', error);
                showAlert('Failed to load dashboard data', 'error');
            });
        }

        /*
        Function: logout
        @params: none
        Description: Logs out the current user
        */
        function logout() {
            fetch('/api/logout', { method: 'POST', credentials: 'include' })
            .then(() => window.location.href = '/index.html')
            .catch(error => {
                console.error('Logout failed:', error);
                window.location.href = '/index.html';
            });
        }

        /*
        Function: showAlert
        @params:
        message: alert message text
        type: alert type (success/error)
        Description: Displays alert message to user
        */
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alertContainer');
            alertContainer.innerHTML = `<div class="alert ${type}">${message}</div>`;
            setTimeout(() => {
                alertContainer.innerHTML = '';
            }, 5000);
        }

        /*
        Function: closeModal
        @params:
        modalId: ID of modal to close
        Description: Closes the specified modal
        */
        function closeModal(modalId) {
            document.getElementById(modalId).style.display = 'none';
        }

        /*
        Function: showAddAnimalModal
        @params: none
        Description: Shows the add animal modal
        */
        function showAddAnimalModal() {
            document.getElementById('addAnimalModal').style.display = 'block';
        }

        /*
        Function: updateAnimalFields
        @params: none
        Description: Updates form fields based on selected animal type
        */
        function updateAnimalFields() {
            const animalType = document.getElementById('animalType').value;
            const container = document.getElementById('typeSpecificFields');
            container.innerHTML = '';
            
            if (animalType === 'dog') {
                container.innerHTML = `
                    <div class="form-group">
                        <label for="breed">Breed</label>
                        <input type="text" id="breed" name="breed" required>
                    </div>
                `;
            } else if (animalType === 'cat') {
                container.innerHTML = `
                    <div class="form-row">
                        <div class="form-group">
                            <label for="breed">Breed</label>
                            <select id="breed" name="breed" required>
                                <option value="">Select Breed</option>
                                <option value="persian">Persian</option>
                                <option value="ragdoll">Ragdoll</option>
                                <option value="maine coon">Maine Coon</option>
                                <option value="british shorthair">British Shorthair</option>
                                <option value="siamese">Siamese</option>
                                <option value="bengal">Bengal</option>
                                <option value="abyssinian">Abyssinian</option>
                                <option value="oriental shorthair">Oriental Shorthair</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="coatColor">Coat Color</label>
                            <input type="text" id="coatColor" name="coatColor" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="declawed">Declawed</label>
                        <select id="declawed" name="declawed">
                            <option value="false">No</option>
                            <option value="true">Yes</option>
                        </select>
                    </div>
                `;
            } else if (animalType === 'monkey') {
                container.innerHTML = `
                    <div class="form-row">
                        <div class="form-group">
                            <label for="species">Species</label>
                            <select id="species" name="species" required>
                                <option value="">Select Species</option>
                                <option value="capuchin">Capuchin</option>
                                <option value="guenon">Guenon</option>
                                <option value="macaque">Macaque</option>
                                <option value="marmoset">Marmoset</option>
                                <option value="squirrel monkey">Squirrel Monkey</option>
                                <option value="tamarin">Tamarin</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="tailLength">Tail Length</label>
                            <input type="text" id="tailLength" name="tailLength" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="height">Height</label>
                            <input type="text" id="height" name="height" required>
                        </div>
                        <div class="form-group">
                            <label for="bodyLength">Body Length</label>
                            <input type="text" id="bodyLength" name="bodyLength" required>
                        </div>
                    </div>
                `;
            } else if (animalType === 'bird') {
                container.innerHTML = `
                    <div class="form-row">
                        <div class="form-group">
                            <label for="species">Species</label>
                            <select id="species" name="species" required>
                                <option value="">Select Species</option>
                                <option value="african grey parrot">African Grey Parrot</option>
                                <option value="cockatiel">Cockatiel</option>
                                <option value="macaw">Macaw</option>
                                <option value="amazon parrot">Amazon Parrot</option>
                                <option value="canary">Canary</option>
                                <option value="finch">Finch</option>
                                <option value="budgerigar">Budgerigar</option>
                                <option value="dove">Dove</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="wingspan">Wingspan</label>
                            <input type="text" id="wingspan" name="wingspan" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="canFly">Can Fly</label>
                            <select id="canFly" name="canFly">
                                <option value="true">Yes</option>
                                <option value="false">No</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="beakType">Beak Type</label>
                            <input type="text" id="beakType" name="beakType" required>
                        </div>
                    </div>
                `;
            } else if (animalType === 'rabbit') {
                container.innerHTML = `
                    <div class="form-row">
                        <div class="form-group">
                            <label for="breed">Breed</label>
                            <select id="breed" name="breed" required>
                                <option value="">Select Breed</option>
                                <option value="holland lop">Holland Lop</option>
                                <option value="mini rex">Mini Rex</option>
                                <option value="lionhead">Lionhead</option>
                                <option value="dutch">Dutch</option>
                                <option value="english angora">English Angora</option>
                                <option value="new zealand">New Zealand</option>
                                <option value="californian">Californian</option>
                                <option value="flemish giant">Flemish Giant</option>
                                <option value="mini lop">Mini Lop</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="furColor">Fur Color</label>
                            <input type="text" id="furColor" name="furColor" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="earType">Ear Type</label>
                            <select id="earType" name="earType" required>
                                <option value="">Select Ear Type</option>
                                <option value="upright">Upright</option>
                                <option value="lopped">Lopped</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="litterTrained">Litter Trained</label>
                            <select id="litterTrained" name="litterTrained">
                                <option value="false">No</option>
                                <option value="true">Yes</option>
                            </select>
                        </div>
                    </div>
                `;
            }
        }

        /*
        Function: handleAddAnimal
        @params:
        event: form submit event
        Description: Handles adding new animal to the system
        */
        function handleAddAnimal(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const data = {};
            formData.forEach((value, key) => {
                data[key] = value;
            });

            fetch('/api/animals', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(data),
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert('Animal added successfully!', 'success');
                    closeModal('addAnimalModal');
                    document.getElementById('addAnimalForm').reset();
                    document.getElementById('typeSpecificFields').innerHTML = '';
                    loadDashboardData();
                } else {
                    showAlert(result.message || 'Failed to add animal', 'error');
                }
            })
            .catch(error => {
                console.error('Add animal error:', error);
                showAlert('Failed to add animal. Please try again.', 'error');
            });
        }

        /*
        Function: viewAllAnimals
        @params: none
        Description: Shows modal with all animals
        */
        function viewAllAnimals() {
            document.getElementById('viewAnimalsTitle').textContent = 'All Animals';
            showAnimalsInModal('all');
        }

        /*
        Function: viewAvailableAnimals
        @params: none
        Description: Shows modal with available animals only
        */
        function viewAvailableAnimals() {
            document.getElementById('viewAnimalsTitle').textContent = 'Available Animals';
            showAnimalsInModal('available');
        }

        /*
        Function: showAnimalsInModal
        @params:
        filter: filter type (all/available)
        Description: Shows animals in modal with optional filtering
        */
        function showAnimalsInModal(filter) {
            document.getElementById('viewAnimalsModal').style.display = 'block';
            if (!allAnimalsData) {
                loadDashboardData();
                return;
            }

            let animals = [];
            if (allAnimalsData.dogs) animals = animals.concat(allAnimalsData.dogs.map(a => ({...a, type: 'Dog'})));
            if (allAnimalsData.monkeys) animals = animals.concat(allAnimalsData.monkeys.map(a => ({...a, type: 'Monkey'})));
            if (allAnimalsData.cats) animals = animals.concat(allAnimalsData.cats.map(a => ({...a, type: 'Cat'})));
            if (allAnimalsData.birds) animals = animals.concat(allAnimalsData.birds.map(a => ({...a, type: 'Bird'})));
            if (allAnimalsData.rabbits) animals = animals.concat(allAnimalsData.rabbits.map(a => ({...a, type: 'Rabbit'})));

            if (filter === 'available') {
                animals = animals.filter(animal => 
                    animal.trainingStatus === 'in service' && !animal.reserved
                );
            }

            window.currentAnimals = animals;
            displayAnimals(animals, 'animalsListContainer');
        }

        /*
        Function: displayAnimals
        @params:
        animals: array of animal objects
        containerId: ID of container to display animals in
        Description: Displays animals in the specified container
        */
        function displayAnimals(animals, containerId) {
            const container = document.getElementById(containerId);
            container.innerHTML = '';

            if (animals.length === 0) {
                container.innerHTML = '<div class="loading">No animals found.</div>';
                return;
            }

            animals.forEach(animal => {
                const animalDiv = document.createElement('div');
                animalDiv.className = 'animal-item';
                
                const imagePath = `/images/${animal.type.toLowerCase()}s/${animal.name}_${animal.age}.jpg`;
                const placeholderClass = `${animal.type.toLowerCase()}-placeholder`;
                const typeInitial = animal.type.charAt(0).toUpperCase();
                
                animalDiv.innerHTML = `
                    <div class="animal-display">
                        <div class="animal-image">
                            <img src="${imagePath}" alt="${animal.name}" 
                                 onload="this.style.display='block'; this.nextElementSibling.style.display='none';"
                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                                 style="display: none;">
                            <div class="animal-placeholder ${placeholderClass}" style="display: flex;">
                                ${typeInitial}
                            </div>
                        </div>
                        <div class="animal-info">
                            <h4>${animal.name} (${animal.type})</h4>
                            <p><strong>Gender:</strong> ${animal.gender} | <strong>Age:</strong> ${animal.age} years</p>
                            <p><strong>Status:</strong> ${animal.trainingStatus} | <strong>Reserved:</strong> ${animal.reserved ? 'Yes' : 'No'}</p>
                            <p><strong>Location:</strong> ${animal.location || 'Not set'}</p>
                            ${getAnimalSpecificInfo(animal)}
                        </div>
                    </div>
                    ${containerId === 'manageAnimalsContainer' && roleHierarchy[currentUser.role] >= roleHierarchy['STAFF'] ? 
                        `<div class="user-actions">
                            <button class="btn btn-primary" onclick="editAnimal('${animal.name}')">Edit</button>
                            ${roleHierarchy[currentUser.role] >= roleHierarchy['ADMIN'] ? 
                                `<button class="btn btn-danger" onclick="deleteAnimal('${animal.name}')">Delete</button>` : ''}
                        </div>` : 
                        ''}
                `;
                container.appendChild(animalDiv);
            });
        }

        /*
        Function: getAnimalSpecificInfo
        @params:
        animal: animal object
        Description: Returns HTML string with animal-specific information
        */
        function getAnimalSpecificInfo(animal) {
            let info = '';
            if (animal.breed) {
                info += `<p><strong>Breed:</strong> ${animal.breed}</p>`;
            }
            if (animal.species) {
                info += `<p><strong>Species:</strong> ${animal.species}</p>`;
            }
            if (animal.coatColor) {
                info += `<p><strong>Color:</strong> ${animal.coatColor}</p>`;
            }
            if (animal.furColor) {
                info += `<p><strong>Fur Color:</strong> ${animal.furColor}</p>`;
            }
            if (animal.wingspan) {
                info += `<p><strong>Wingspan:</strong> ${animal.wingspan}</p>`;
            }
            return info;
        }

        /*
        Function: applySorting
        @params: none
        Description: Applies sorting and filtering to current animals list
        */
        function applySorting() {
            if (!window.currentAnimals) return;
            
            const sortBy = document.getElementById('sortBy').value;
            const sortOrder = document.getElementById('sortOrder').value;
            const typeFilter = document.getElementById('typeFilter').value;
            
            let filteredAnimals = [...window.currentAnimals];
            
            if (typeFilter) {
                filteredAnimals = filteredAnimals.filter(animal => animal.type === typeFilter);
            }
            
            filteredAnimals.sort((a, b) => {
                let aVal = a[sortBy];
                let bVal = b[sortBy];
                
                if (sortBy === 'age') {
                    aVal = parseInt(aVal) || 0;
                    bVal = parseInt(bVal) || 0;
                }
                
                if (typeof aVal === 'string') aVal = aVal.toLowerCase();
                if (typeof bVal === 'string') bVal = bVal.toLowerCase();
                
                if (sortOrder === 'asc') {
                    return aVal > bVal ? 1 : aVal < bVal ? -1 : 0;
                } else {
                    return aVal < bVal ? 1 : aVal > bVal ? -1 : 0;
                }
            });
            
            displayAnimals(filteredAnimals, 'animalsListContainer');
        }

        /*
        Function: applyManagementSorting
        @params: none
        Description: Applies sorting and filtering to management animals list
        */
        function applyManagementSorting() {
            if (!window.currentManageAnimals) return;
            
            const sortBy = document.getElementById('manageSortBy').value;
            const sortOrder = document.getElementById('manageSortOrder').value;
            const typeFilter = document.getElementById('manageTypeFilter').value;
            
            let filteredAnimals = [...window.currentManageAnimals];
            
            if (typeFilter) {
                filteredAnimals = filteredAnimals.filter(animal => animal.type === typeFilter);
            }
            
            filteredAnimals.sort((a, b) => {
                let aVal = a[sortBy];
                let bVal = b[sortBy];
                
                if (sortBy === 'age') {
                    aVal = parseInt(aVal) || 0;
                    bVal = parseInt(bVal) || 0;
                }
                
                if (typeof aVal === 'string') aVal = aVal.toLowerCase();
                if (typeof bVal === 'string') bVal = bVal.toLowerCase();
                
                if (sortOrder === 'asc') {
                    return aVal > bVal ? 1 : aVal < bVal ? -1 : 0;
                } else {
                    return aVal < bVal ? 1 : aVal > bVal ? -1 : 0;
                }
            });
            
            displayAnimals(filteredAnimals, 'manageAnimalsContainer');
        }

        /*
        Function: showManageAnimalsModal
        @params: none
        Description: Shows the animal management modal
        */
        function showManageAnimalsModal() {
            document.getElementById('manageAnimalsModal').style.display = 'block';
            loadAnimalsForManagement();
        }

        /*
        Function: loadAnimalsForManagement
        @params: none
        Description: Loads animals for the management interface
        */
        function loadAnimalsForManagement() {
            if (!allAnimalsData) {
                loadDashboardData();
                return;
            }

            let animals = [];
            if (allAnimalsData.dogs) animals = animals.concat(allAnimalsData.dogs.map(a => ({...a, type: 'Dog'})));
            if (allAnimalsData.monkeys) animals = animals.concat(allAnimalsData.monkeys.map(a => ({...a, type: 'Monkey'})));
            if (allAnimalsData.cats) animals = animals.concat(allAnimalsData.cats.map(a => ({...a, type: 'Cat'})));
            if (allAnimalsData.birds) animals = animals.concat(allAnimalsData.birds.map(a => ({...a, type: 'Bird'})));
            if (allAnimalsData.rabbits) animals = animals.concat(allAnimalsData.rabbits.map(a => ({...a, type: 'Rabbit'})));

            window.currentManageAnimals = animals;
            displayAnimals(animals, 'manageAnimalsContainer');
        }

        /*
        Function: deleteAnimal
        @params:
        animalName: name of animal to delete
        Description: Deletes an animal from the system
        */
        function deleteAnimal(animalName) {
            if (!confirm(`Are you sure you want to delete ${animalName}? This action cannot be undone.`)) {
                return;
            }

            fetch(`/api/animals?name=${encodeURIComponent(animalName)}`, {
                method: 'DELETE',
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert(`${animalName} deleted successfully`, 'success');
                    loadDashboardData();
                    loadAnimalsForManagement();
                } else {
                    showAlert(result.message || 'Failed to delete animal', 'error');
                }
            })
            .catch(error => {
                console.error('Delete animal error:', error);
                showAlert('Failed to delete animal. Please try again.', 'error');
            });
        }

        /*
        Function: showReserveModal
        @params: none
        Description: Shows the animal reservation modal
        */
        function showReserveModal() {
            document.getElementById('reserveModal').style.display = 'block';
        }

        /*
        Function: handleReservation
        @params:
        event: form submit event
        Description: Handles animal reservation requests
        */
        function handleReservation(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const data = {};
            formData.forEach((value, key) => {
                data[key] = value;
            });

            fetch('/api/reserve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(data),
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert('Animal reserved successfully!', 'success');
                    closeModal('reserveModal');
                    document.getElementById('reserveForm').reset();
                    loadDashboardData();
                } else {
                    showAlert(result.message || 'Failed to reserve animal', 'error');
                }
            })
            .catch(error => {
                console.error('Reserve animal error:', error);
                showAlert('Failed to reserve animal. Please try again.', 'error');
            });
        }

        /*
        Function: viewActivities
        @params: none
        Description: Shows activity log in alert dialog
        */
        function viewActivities() {
            fetch('/api/activities', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                let activitiesHtml = 'Recent Activities:\\n\\n';
                if (data.activities && data.activities.length > 0) {
                    data.activities.slice(0, 10).forEach(activity => {
                        activitiesHtml += `${activity.animalName} - ${activity.description} (by ${activity.performedBy})\\n`;
                    });
                } else {
                    activitiesHtml += 'No activities found.';
                }
                alert(activitiesHtml);
            })
            .catch(error => {
                showAlert('Failed to load activities', 'error');
            });
        }

        /*
        Function: editAnimal
        @params:
        animalName: name of animal to edit
        Description: Opens edit modal for specified animal
        */
        function editAnimal(animalName) {
            let animal = null;
            if (allAnimalsData.dogs) animal = allAnimalsData.dogs.find(a => a.name === animalName);
            if (!animal && allAnimalsData.monkeys) animal = allAnimalsData.monkeys.find(a => a.name === animalName);
            if (!animal && allAnimalsData.cats) animal = allAnimalsData.cats.find(a => a.name === animalName);
            if (!animal && allAnimalsData.birds) animal = allAnimalsData.birds.find(a => a.name === animalName);
            if (!animal && allAnimalsData.rabbits) animal = allAnimalsData.rabbits.find(a => a.name === animalName);
            
            if (!animal) {
                showAlert('Animal not found', 'error');
                return;
            }

            document.getElementById('editAnimalOriginalName').value = animal.name;
            document.getElementById('editAnimalName').value = animal.name;
            document.getElementById('editAnimalGender').value = animal.gender;
            document.getElementById('editAnimalAge').value = animal.age;
            document.getElementById('editAnimalWeight').value = animal.weight;
            document.getElementById('editTrainingStatus').value = animal.trainingStatus;
            document.getElementById('editReserved').value = animal.reserved.toString();
            document.getElementById('editAnimalLocation').value = animal.location || '';

            document.getElementById('editAnimalModal').style.display = 'block';
        }

        /*
        Function: handleEditAnimal
        @params:
        event: form submit event
        Description: Handles animal edit form submission
        */
        function handleEditAnimal(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const data = {};
            formData.forEach((value, key) => {
                data[key] = value;
            });

            fetch('/api/animals', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(data),
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert('Animal updated successfully!', 'success');
                    closeModal('editAnimalModal');
                    loadDashboardData();
                    if (window.currentManageAnimals) {
                        loadAnimalsForManagement();
                    }
                } else {
                    showAlert(result.message || 'Failed to update animal', 'error');
                }
            })
            .catch(error => {
                console.error('Edit animal error:', error);
                showAlert('Failed to update animal. Please try again.', 'error');
            });
        }

        /*
        Function: showUserManagementModal
        @params: none
        Description: Shows the user management modal
        */
        function showUserManagementModal() {
            document.getElementById('userManagementModal').style.display = 'block';
            loadUsers();
        }

        /*
        Function: switchTab
        @params:
        tabName: name of tab to switch to
        Description: Switches between tabs in user management modal
        */
        function switchTab(tabName) {
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            
            document.querySelectorAll('.tab').forEach(tab => {
                tab.classList.remove('active');
            });
            
            document.getElementById(tabName).classList.add('active');
            event.target.classList.add('active');
            
            if (tabName === 'usersList') {
                loadUsers();
            }
        }

        /*
        Function: loadUsers
        @params: none
        Description: Loads user list from server
        */
        function loadUsers() {
            fetch('/api/users', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                allUsersData = data.users;
                displayUsers(data.users);
            })
            .catch(error => {
                console.error('Failed to load users:', error);
                showAlert('Failed to load users', 'error');
            });
        }

        /*
        Function: displayUsers
        @params:
        users: array of user objects
        Description: Displays users in the user management interface
        */
        function displayUsers(users) {
            const container = document.getElementById('usersListContainer');
            container.innerHTML = '';

            if (users.length === 0) {
                container.innerHTML = '<div class="loading">No users found.</div>';
                return;
            }

            users.forEach(user => {
                const userDiv = document.createElement('div');
                userDiv.className = 'user-item';
                
                const roleClass = `role-${user.role.toLowerCase()}`;
                const statusClass = user.active ? 'status-active' : 'status-inactive';
                const statusText = user.active ? 'Active' : 'Inactive';
                const userInitials = user.fullName.split(' ').map(n => n[0]).join('').toUpperCase();
                
                userDiv.innerHTML = `
                    <div class="user-display">
                        <div class="user-avatar">
                            ${userInitials}
                        </div>
                        <div class="user-info-detail">
                            <h4>${user.fullName} (${user.username})</h4>
                            <p>
                                <span class="role-badge ${roleClass}">${user.role}</span>
                                <span class="status-badge ${statusClass}">${statusText}</span>
                            </p>
                        </div>
                    </div>
                    <div class="user-actions">
                        <button class="btn btn-primary" onclick="editUser('${user.username}')">Edit</button>
                        <button class="btn btn-warning" onclick="toggleUserStatus('${user.username}')">${user.active ? 'Deactivate' : 'Activate'}</button>
                        ${user.username !== 'admin' && user.username !== currentUser.username ? 
                            `<button class="btn btn-danger" onclick="deleteUser('${user.username}')">Delete</button>` : 
                            ''}
                    </div>
                `;
                container.appendChild(userDiv);
            });
        }

        /*
        Function: handleCreateUser
        @params:
        event: form submit event
        Description: Handles new user creation
        */
        function handleCreateUser(event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const data = {};
            formData.forEach((value, key) => {
                data[key] = value;
            });

            fetch('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(data),
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert('User created successfully!', 'success');
                    document.getElementById('createUserForm').reset();
                    loadUsers();
                    switchTab('usersList');
                } else {
                    showAlert(result.message || 'Failed to create user', 'error');
                }
            })
            .catch(error => {
                console.error('Create user error:', error);
                showAlert('Failed to create user. Please try again.', 'error');
            });
        }

        /*
        Function: editUser
        @params:
        username: username of user to edit
        Description: Opens edit modal for specified user
        */
        function editUser(username) {
            const user = allUsersData.find(u => u.username === username);
            if (!user) {
                showAlert('User not found', 'error');
                return;
            }

            document.getElementById('editUsername').value = user.username;
            document.getElementById('editFullName').value = user.fullName;
            document.getElementById('editRole').value = user.role;
            document.getElementById('newUserPassword').value = '';
            
            document.getElementById('editUserModal').style.display = 'block';
        }

        /*
        Function: handleEditUser
        @params:
        event: form submit event
        Description: Handles user edit form submission with full name and role updates
        */
        function handleEditUser(event) {
            event.preventDefault();
            console.log('handleEditUser called');
            
            const formData = new FormData(event.target);
            const username = formData.get('username');
            const fullName = formData.get('fullName');
            const role = formData.get('role');
            const newPassword = formData.get('newPassword');
            
            console.log('Form data captured:', { username, fullName, role, hasPassword: !!newPassword });
            
            if (!username || !fullName || !role) {
                showAlert('All fields except password are required', 'error');
                return;
            }
            
            // STEP 1: Update full name (THIS IS MISSING IN YOUR CURRENT CODE)
            console.log('Step 1: Updating full name...');
            fetch('/api/users/fullname', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ username, fullName }),
                credentials: 'include'
            })
            .then(response => {
                console.log('Full name response status:', response.status);
                return response.json();
            })
            .then(result => {
                console.log('Full name update result:', result);
                if (result.success) {
                    // STEP 2: Update role
                    console.log('Step 2: Updating role...');
                    return fetch('/api/users/role', {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: new URLSearchParams({ username, role }),
                        credentials: 'include'
                    });
                } else {
                    throw new Error(result.message || 'Failed to update full name');
                }
            })
            .then(response => {
                console.log('Role response status:', response.status);
                return response.json();
            })
            .then(result => {
                console.log('Role update result:', result);
                if (result.success) {
                    // STEP 3: Update password if provided
                    if (newPassword && newPassword.length >= 6) {
                        console.log('Step 3: Updating password...');
                        return fetch('/api/users/password', {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: new URLSearchParams({ username, newPassword }),
                            credentials: 'include'
                        });
                    } else {
                        console.log('Step 3: No password update needed');
                        return { ok: true, json: () => ({ success: true }) };
                    }
                } else {
                    throw new Error(result.message || 'Failed to update role');
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Password update failed');
            })
            .then(result => {
                console.log('All updates complete:', result);
                if (result.success) {
                    showAlert('User updated successfully!', 'success');
                    closeModal('editUserModal');
                    loadUsers(); // This will show the updated full name
                } else {
                    showAlert(result.message || 'Failed to complete updates', 'error');
                }
            })
            .catch(error => {
                console.error('Error updating user:', error);
                showAlert('Error updating user: ' + error.message, 'error');
            });
        }

        /*
        Function: toggleUserStatus
        @params:
        username: username of user to toggle status
        Description: Toggles user active/inactive status
        */
        function toggleUserStatus(username) {
            const user = allUsersData.find(u => u.username === username);
            const action = user.active ? 'deactivate' : 'activate';
            
            if (!confirm(`Are you sure you want to ${action} user ${username}?`)) {
                return;
            }

            fetch('/api/users/status', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ username }),
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert(`User ${action}d successfully!`, 'success');
                    loadUsers();
                } else {
                    showAlert(result.message || `Failed to ${action} user`, 'error');
                }
            })
            .catch(error => {
                console.error('Toggle user status error:', error);
                showAlert(`Failed to ${action} user. Please try again.`, 'error');
            });
        }

        /*
        Function: deleteUser
        @params:
        username: username of user to delete
        Description: Deletes a user from the system
        */
        function deleteUser(username) {
            if (!confirm(`Are you sure you want to delete user ${username}? This action cannot be undone.`)) {
                return;
            }

            fetch(`/api/users?username=${encodeURIComponent(username)}`, {
                method: 'DELETE',
                credentials: 'include'
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showAlert('User deleted successfully!', 'success');
                    loadUsers();
                } else {
                    showAlert(result.message || 'Failed to delete user', 'error');
                }
            })
            .catch(error => {
                console.error('Delete user error:', error);
                showAlert('Failed to delete user. Please try again.', 'error');
            });
        }

        /*
        Function: viewSessions
        @params: none
        Description: Shows active user sessions in alert dialog
        */
        function viewSessions() {
            fetch('/api/sessions', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                let sessionsHtml = 'Active User Sessions:\\n\\n';
                if (data.sessions && data.sessions.length > 0) {
                    data.sessions.forEach(session => {
                        const createdDate = new Date(session.createdTime).toLocaleString();
                        const lastAccessDate = new Date(session.lastAccess).toLocaleString();
                        sessionsHtml += `${session.fullName} (${session.username})\\nSession: ${session.sessionId}\\nCreated: ${createdDate}\\nLast Access: ${lastAccessDate}\\n\\n`;
                    });
                } else {
                    sessionsHtml += 'No active sessions found.';
                }
                alert(sessionsHtml);
            })
            .catch(error => {
                showAlert('Failed to load sessions', 'error');
            });
        }

        /*
        Function: window.onclick
        @params:
        event: click event
        Description: Closes modals when clicking outside them
        */
        window.onclick = function(event) {
            const modals = document.querySelectorAll('.modal');
            modals.forEach(modal => {
                if (event.target === modal) {
                    modal.style.display = 'none';
                }
            });
        }

        /*
        Function: setInterval
        @params: none
        Description: Auto-refresh dashboard data every 30 seconds
        */
        setInterval(loadDashboardData, 30000);
    </script>
</body>
</html>
""";
    }
    
    /*
    Function: displayStartupInformation
    @params:
    port: server port number
    Description: Displays startup information to console
    */
    private static void displayStartupInformation(int port) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("WEB SERVER CONFIGURATION");
        System.out.println("=".repeat(60));
        System.out.println("Port: " + port);
        System.out.println("Web root: ./web/");
        System.out.println("Data directory: ./data/");
        System.out.println();
        
        System.out.println("DEFAULT ACCOUNTS:");
        System.out.println("Admin: admin / admin123 (Full access)");
        System.out.println("Staff: staff_user / staff123 (Animal management)");
        System.out.println("Monitor: monitor_user / monitor123 (View + activities)");
        System.out.println("Viewer: view_user / view123 (View only)");
        System.out.println();
        
        System.out.println("APPLICATION URLs:");
        System.out.println("Main: http://localhost:" + port);
        System.out.println("Login: http://localhost:" + port + "/index.html");
        System.out.println("Dashboard: http://localhost:" + port + "/dashboard.html");
        System.out.println();
        
        System.out.println("Starting server...");
    }
    
    /*
    Function: setupShutdownHook
    @params: none
    Description: Sets up graceful shutdown handling
    */
    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down gracefully...");
            shutdown();
        }));
    }
    
    /*
    Function: shutdown
    @params: none
    Description: Gracefully shuts down the application
    */
    public static void shutdown() {
        if (webServer != null) {
            webServer.stop();
        }
        if (dataManager != null) {
            dataManager.saveAll();
        }
        System.out.println("Application shutdown complete");
    }
}