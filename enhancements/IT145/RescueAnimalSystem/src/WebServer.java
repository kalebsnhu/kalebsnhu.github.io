import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.net.httpserver.*;

public class WebServer {
    private int port;
    private HttpServer server;
    private boolean running = false;
    
    private AuthenticationSystem authSystem;
    private AnimalService animalService;
    private MonitoringSystem monitoringSystem;
    private ReservationService reservationService;
    private SimpleDataManager dataManager;
    private Map<String, String> sessionMap = new ConcurrentHashMap<>();
    
    /*
    Function: WebServer constructor
    @params:
    port: server port number
    authSystem: authentication system instance
    animalService: animal service instance
    monitoringSystem: monitoring system instance
    reservationService: reservation service instance
    dataManager: data manager instance
    Description: Initializes WebServer with all required dependencies
    */
    public WebServer(int port, AuthenticationSystem authSystem, AnimalService animalService,
                    MonitoringSystem monitoringSystem, ReservationService reservationService,
                    SimpleDataManager dataManager) {
        this.port = port;
        this.authSystem = authSystem;
        this.animalService = animalService;
        this.monitoringSystem = monitoringSystem;
        this.reservationService = reservationService;
        this.dataManager = dataManager;
    }
    
    /*
    Function: start
    @params: none
    Description: Starts the HTTP server and sets up all endpoints
    */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            server.createContext("/", this::handleStaticFile);
            server.createContext("/css/", this::handleStaticFile);
            server.createContext("/js/", this::handleStaticFile);
            server.createContext("/images/", this::handleImageFile);
            
            server.createContext("/api/login", this::handleLogin);
            server.createContext("/api/logout", this::handleLogout);
            server.createContext("/api/register", this::handleRegister);
            server.createContext("/api/user", this::handleUser);
            
            server.createContext("/api/animals", this::handleAnimals);
            server.createContext("/api/activities", this::handleActivities);
            server.createContext("/api/reserve", this::handleReservation);
            
            server.createContext("/api/users", this::handleUsers);
            server.createContext("/api/users/fullname", this::handleUserFullName);
            server.createContext("/api/users/role", this::handleUserRole);
            server.createContext("/api/users/password", this::handleUserPassword);
            server.createContext("/api/users/status", this::handleUserStatus);
            server.createContext("/api/sessions", this::handleSessions);
            
            server.setExecutor(null);
            server.start();
            running = true;
            
            System.out.println("Web server started on http://localhost:" + port);
            System.out.println("Default admin login: admin / admin123");
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    
    /*
    Function: stop
    @params: none
    Description: Stops the HTTP server
    */
    public void stop() {
        if (server != null) {
            server.stop(0);
            running = false;
            System.out.println("Web server stopped");
        }
    }
    
    /*
    Function: handleImageFile
    @params:
    exchange: HTTP exchange object
    Description: Serves image files from the images directory
    */
    private void handleImageFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        File file = new File("images" + path.substring(7));
        if (file.exists() && file.isFile()) {
            String contentType = getContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
            
            FileInputStream fis = new FileInputStream(file);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            
            fis.close();
            os.close();
        } else {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().close();
        }
    }
    
    /*
    Function: handleStaticFile
    @params:
    exchange: HTTP exchange object
    Description: Serves static files from the web directory
    */
    private void handleStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        
        File file = new File("web" + path);
        if (file.exists() && file.isFile()) {
            String contentType = getContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            FileInputStream fis = new FileInputStream(file);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            
            fis.close();
            os.close();
        } else {
            send404(exchange);
        }
    }

    /*
    Function: handleUserFullName
    @params:
    exchange: HTTP exchange object
    Description: Handles user full name update requests (admin only)
    */
    private void handleUserFullName(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
            return;
        }
        
        if ("PUT".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String fullName = params.get("fullName");
            
            if (username == null || fullName == null || fullName.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username and full name required\"}");
                return;
            }
            
            User targetUser = dataManager.getUser(username);
            if (targetUser == null) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"User not found\"}");
                return;
            }
            
            targetUser.setFullName(fullName.trim());
            dataManager.saveAll();
            
            System.out.println(String.format("User full name updated: %s -> %s", username, fullName));
            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"User full name updated successfully\"}");
        }
    }
    
    /*
    Function: handleLogin
    @params:
    exchange: HTTP exchange object
    Description: Handles user login requests and creates sessions
    */
    private void handleLogin(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String password = params.get("password");
            
            SessionData session = authSystem.login(username, password);
            if (session != null) {
                exchange.getResponseHeaders().add("Set-Cookie", 
                    "sessionId=" + session.getSessionId() + "; Path=/; HttpOnly");
                
                sendJsonResponse(exchange, 200, "{\"success\": true, \"redirect\": \"/dashboard.html\"}");
            } else {
                sendJsonResponse(exchange, 401, "{\"success\": false, \"message\": \"Invalid credentials\"}");
            }
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }
    
    /*
    Function: handleLogout
    @params:
    exchange: HTTP exchange object
    Description: Handles user logout and clears sessions
    */
    private void handleLogout(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        if (sessionId != null) {
            authSystem.logout(sessionId);
        }
        
        exchange.getResponseHeaders().add("Set-Cookie", 
            "sessionId=; Path=/; HttpOnly; Max-Age=0");
        
        sendJsonResponse(exchange, 200, "{\"success\": true, \"redirect\": \"/index.html\"}");
    }
    
    /*
    Function: handleRegister
    @params:
    exchange: HTTP exchange object
    Description: Handles user registration requests
    */
    private void handleRegister(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String password = params.get("password");
            String fullName = params.get("fullName");
            
            boolean success = authSystem.createUser(username, password, fullName, UserRole.VIEW);
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Account created successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username already exists\"}");
            }
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }
    
    /*
    Function: handleUser
    @params:
    exchange: HTTP exchange object
    Description: Returns current user information for authenticated sessions
    */
    private void handleUser(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session != null) {
            User user = session.getUser();
            String userJson = String.format(
                "{\"username\": \"%s\", \"fullName\": \"%s\", \"role\": \"%s\"}",
                user.getUsername(), user.getFullName(), user.getRole()
            );
            sendJsonResponse(exchange, 200, userJson);
        } else {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
        }
    }
    
    /*
    Function: handleUsers
    @params:
    exchange: HTTP exchange object
    Description: Handles user management operations (GET, POST, DELETE)
    */
    private void handleUsers(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
                return;
            }
            
            List<User> users = authSystem.getAllUsers(session.getUser());
            StringBuilder json = new StringBuilder();
            json.append("{\"users\": [");
            
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                json.append("{");
                json.append("\"username\": \"").append(user.getUsername()).append("\",");
                json.append("\"fullName\": \"").append(user.getFullName()).append("\",");
                json.append("\"role\": \"").append(user.getRole()).append("\",");
                json.append("\"active\": ").append(user.isActive());
                json.append("}");
                if (i < users.size() - 1) json.append(",");
            }
            
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
            
        } else if ("POST".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
                return;
            }
            
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String password = params.get("password");
            String fullName = params.get("fullName");
            String roleStr = params.get("role");
            
            UserRole role = UserRole.VIEW;
            try {
                if (roleStr != null && !roleStr.isEmpty()) {
                    role = UserRole.valueOf(roleStr.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid role\"}");
                return;
            }
            
            boolean success = authSystem.createUserByAdmin(username, password, fullName, role, session.getUser());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"User created successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to create user\"}");
            }
            
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String username = getQueryParam(query, "username");
            
            if (username != null) {
                boolean success = authSystem.deleteUser(username, session.getUser());
                
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"User deleted successfully\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to delete user\"}");
                }
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username required\"}");
            }
        }
    }
    
    /*
    Function: handleUserRole
    @params:
    exchange: HTTP exchange object
    Description: Handles user role update requests (admin only)
    */
    private void handleUserRole(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
            return;
        }
        
        if ("PUT".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String roleStr = params.get("role");
            
            if (username == null || roleStr == null) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username and role required\"}");
                return;
            }
            
            UserRole role;
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid role\"}");
                return;
            }
            
            boolean success = authSystem.updateUserRole(username, role, session.getUser());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"User role updated successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to update user role\"}");
            }
        }
    }
    
    /*
    Function: handleUserPassword
    @params:
    exchange: HTTP exchange object
    Description: Handles password change/reset requests
    */
    private void handleUserPassword(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if ("PUT".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            String newPassword = params.get("newPassword");
            String oldPassword = params.get("oldPassword");
            
            if (username == null || newPassword == null) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username and new password required\"}");
                return;
            }
            
            boolean success;
            
            if (authSystem.hasPermission(session.getUser(), UserRole.ADMIN) && 
                !session.getUser().getUsername().equals(username)) {
                success = authSystem.resetUserPassword(username, newPassword, session.getUser());
            } else {
                success = authSystem.changePassword(username, oldPassword, newPassword, session.getUser());
            }
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Password updated successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to update password\"}");
            }
        }
    }
    
    /*
    Function: handleUserStatus
    @params:
    exchange: HTTP exchange object
    Description: Handles user status toggle requests (admin only)
    */
    private void handleUserStatus(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
            return;
        }
        
        if ("PUT".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.get("username");
            
            if (username == null) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Username required\"}");
                return;
            }
            
            boolean success = authSystem.toggleUserStatus(username, session.getUser());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"User status updated successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to update user status\"}");
            }
        }
    }
    
    /*
    Function: handleSessions
    @params:
    exchange: HTTP exchange object
    Description: Returns active session information (admin only)
    */
    private void handleSessions(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<AuthenticationSystem.SessionInfo> sessions = authSystem.getActiveSessions(session.getUser());
            StringBuilder json = new StringBuilder();
            json.append("{\"sessions\": [");
            
            for (int i = 0; i < sessions.size(); i++) {
                AuthenticationSystem.SessionInfo sessionInfo = sessions.get(i);
                json.append("{");
                json.append("\"sessionId\": \"").append(sessionInfo.getSessionId()).append("\",");
                json.append("\"username\": \"").append(sessionInfo.getUsername()).append("\",");
                json.append("\"fullName\": \"").append(sessionInfo.getFullName()).append("\",");
                json.append("\"createdTime\": ").append(sessionInfo.getCreatedTime()).append(",");
                json.append("\"lastAccess\": ").append(sessionInfo.getLastAccess());
                json.append("}");
                if (i < sessions.size() - 1) json.append(",");
            }
            
            json.append("]}");
            sendJsonResponse(exchange, 200, json.toString());
        }
    }
    
    /*
    Function: handleAnimals
    @params:
    exchange: HTTP exchange object
    Description: Handles animal management operations (GET, POST, PUT, DELETE)
    */
    private void handleAnimals(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"dogs\": ").append(serializeDogs()).append(",");
            json.append("\"monkeys\": ").append(serializeMonkeys()).append(",");
            json.append("\"cats\": ").append(serializeCats()).append(",");
            json.append("\"birds\": ").append(serializeBirds()).append(",");
            json.append("\"rabbits\": ").append(serializeRabbits()).append(",");
            json.append("\"stats\": ").append(getStatsJson());
            json.append("}");
            
            sendJsonResponse(exchange, 200, json.toString());
            
        } else if ("POST".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.STAFF)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Staff access required\"}");
                return;
            }
            
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            boolean success = addAnimalFromParams(params, session.getUser().getUsername());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Animal added successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to add animal\"}");
            }
            
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.STAFF)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Staff access required\"}");
                return;
            }
            
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            boolean success = updateAnimalFromParams(params, session.getUser().getUsername());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Animal updated successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Failed to update animal\"}");
            }
            
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            if (!authSystem.hasPermission(session.getUser(), UserRole.ADMIN)) {
                sendJsonResponse(exchange, 403, "{\"error\": \"Admin access required\"}");
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String animalName = getQueryParam(query, "name");
            
            if (animalName != null) {
                boolean success = dataManager.removeAnimal(animalName);
                animalService.refreshFromDatabase();
                
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Animal deleted successfully\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Animal not found\"}");
                }
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Animal name required\"}");
            }
        }
    }
    
    /*
    Function: handleActivities
    @params:
    exchange: HTTP exchange object
    Description: Returns activity log data (monitor access required)
    */
    private void handleActivities(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.MONITOR)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Monitor access required\"}");
            return;
        }
        
        List<Activity> activities = dataManager.getActivities();
        StringBuilder json = new StringBuilder();
        json.append("{\"activities\": [");
        
        for (int i = 0; i < activities.size(); i++) {
            Activity activity = activities.get(i);
            json.append("{");
            json.append("\"animalName\": \"").append(activity.getAnimalName()).append("\",");
            json.append("\"animalType\": \"").append(activity.getAnimalType()).append("\",");
            json.append("\"activityType\": \"").append(activity.getActivityType()).append("\",");
            json.append("\"description\": \"").append(activity.getDescription()).append("\",");
            json.append("\"location\": \"").append(activity.getLocation()).append("\",");
            json.append("\"performedBy\": \"").append(activity.getPerformedBy()).append("\",");
            json.append("\"timestamp\": \"").append(activity.getTimestamp()).append("\"");
            json.append("}");
            if (i < activities.size() - 1) json.append(",");
        }
        
        json.append("]}");
        sendJsonResponse(exchange, 200, json.toString());
    }
    
    /*
    Function: handleReservation
    @params:
    exchange: HTTP exchange object
    Description: Handles animal reservation requests (staff access required)
    */
    private void handleReservation(HttpExchange exchange) throws IOException {
        String sessionId = getSessionFromCookie(exchange);
        SessionData session = authSystem.validateSession(sessionId);
        
        if (session == null) {
            sendJsonResponse(exchange, 401, "{\"error\": \"Not authenticated\"}");
            return;
        }
        
        if (!authSystem.hasPermission(session.getUser(), UserRole.STAFF)) {
            sendJsonResponse(exchange, 403, "{\"error\": \"Staff access required\"}");
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String animalType = params.get("animalType");
            String serviceCountry = params.get("serviceCountry");
            
            boolean success = reservationService.reserveAnimal(animalType, serviceCountry, 
                                                             session.getUser().getUsername());
            
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Animal reserved successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"No available animals found\"}");
            }
        }
    }
    
    /*
    Function: getSessionFromCookie
    @params:
    exchange: HTTP exchange object
    Description: Extracts session ID from cookie header
    */
    private String getSessionFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && "sessionId".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }
    
    /*
    Function: readRequestBody
    @params:
    exchange: HTTP exchange object
    Description: Reads and returns the request body as a string
    */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }
    
    /*
    Function: parseFormData
    @params:
    formData: URL-encoded form data string
    Description: Parses form data into a key-value map
    */
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        for (String pair : formData.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    params.put(URLDecoder.decode(keyValue[0], "UTF-8"), 
                              URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // Ignore malformed pairs
                }
            }
        }
        return params;
    }
    
    /*
    Function: getQueryParam
    @params:
    query: query string
    param: parameter name to extract
    Description: Extracts a specific parameter from query string
    */
    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && param.equals(keyValue[0])) {
                try {
                    return URLDecoder.decode(keyValue[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            }
        }
        return null;
    }
    
    /*
    Function: sendJsonResponse
    @params:
    exchange: HTTP exchange object
    status: HTTP status code
    json: JSON response body
    Description: Sends a JSON response with proper headers
    */
    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, json.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }
    
    /*
    Function: send404
    @params:
    exchange: HTTP exchange object
    Description: Sends a 404 Not Found response
    */
    private void send404(HttpExchange exchange) throws IOException {
        String response = "404 Not Found";
        exchange.sendResponseHeaders(404, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    /*
    Function: getContentType
    @params:
    path: file path
    Description: Returns appropriate MIME type for file extension
    */
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        return "text/plain";
    }
    
    /*
    Function: updateAnimalFromParams
    @params:
    params: form parameters map
    updatedBy: username of person making update
    Description: Updates animal record from form data
    */
    private boolean updateAnimalFromParams(Map<String, String> params, String updatedBy) {
        String originalName = params.get("originalName");
        String newName = params.get("name");
        
        if (originalName == null || newName == null) {
            return false;
        }
        
        try {
            RescueAnimal animal = dataManager.findAnimalByName(originalName);
            if (animal == null) {
                return false;
            }
            
            animal.setName(newName);
            animal.setGender(params.get("gender"));
            animal.setAge(params.get("age"));
            animal.setWeight(params.get("weight"));
            animal.setTrainingStatus(params.get("trainingStatus"));
            animal.setReserved(Boolean.parseBoolean(params.getOrDefault("reserved", "false")));
            
            String location = params.get("location");
            if (location != null && !location.trim().isEmpty()) {
                monitoringSystem.updateAnimalLocation(newName, location, updatedBy);
            }
            
            dataManager.saveAll();
            animalService.refreshFromDatabase();
            
            monitoringSystem.logActivity(newName, animal.getAnimalType(), "UPDATE", 
                                       "Animal information updated", 
                                       monitoringSystem.getAnimalLocation(newName), updatedBy);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error updating animal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /*
    Function: serializeDogs
    @params: none
    Description: Converts dog list to JSON string
    */
    private String serializeDogs() {
        StringBuilder json = new StringBuilder("[");
        List<Dog> dogs = animalService.getDogList();
        for (int i = 0; i < dogs.size(); i++) {
            Dog dog = dogs.get(i);
            json.append("{");
            json.append("\"name\": \"").append(dog.getName()).append("\",");
            json.append("\"breed\": \"").append(dog.getBreed()).append("\",");
            json.append("\"gender\": \"").append(dog.getGender()).append("\",");
            json.append("\"age\": \"").append(dog.getAge()).append("\",");
            json.append("\"weight\": \"").append(dog.getWeight()).append("\",");
            json.append("\"trainingStatus\": \"").append(dog.getTrainingStatus()).append("\",");
            json.append("\"reserved\": ").append(dog.getReserved()).append(",");
            json.append("\"location\": \"").append(monitoringSystem.getAnimalLocation(dog.getName())).append("\"");
            json.append("}");
            if (i < dogs.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    /*
    Function: serializeMonkeys
    @params: none
    Description: Converts monkey list to JSON string
    */
    private String serializeMonkeys() {
        StringBuilder json = new StringBuilder("[");
        List<Monkey> monkeys = animalService.getMonkeyList();
        for (int i = 0; i < monkeys.size(); i++) {
            Monkey monkey = monkeys.get(i);
            json.append("{");
            json.append("\"name\": \"").append(monkey.getName()).append("\",");
            json.append("\"species\": \"").append(monkey.getSpecies()).append("\",");
            json.append("\"gender\": \"").append(monkey.getGender()).append("\",");
            json.append("\"age\": \"").append(monkey.getAge()).append("\",");
            json.append("\"weight\": \"").append(monkey.getWeight()).append("\",");
            json.append("\"trainingStatus\": \"").append(monkey.getTrainingStatus()).append("\",");
            json.append("\"reserved\": ").append(monkey.getReserved()).append(",");
            json.append("\"location\": \"").append(monitoringSystem.getAnimalLocation(monkey.getName())).append("\"");
            json.append("}");
            if (i < monkeys.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    /*
    Function: serializeCats
    @params: none
    Description: Converts cat list to JSON string
    */
    private String serializeCats() {
        StringBuilder json = new StringBuilder("[");
        List<Cat> cats = animalService.getCatList();
        for (int i = 0; i < cats.size(); i++) {
            Cat cat = cats.get(i);
            json.append("{");
            json.append("\"name\": \"").append(cat.getName()).append("\",");
            json.append("\"breed\": \"").append(cat.getBreed()).append("\",");
            json.append("\"gender\": \"").append(cat.getGender()).append("\",");
            json.append("\"age\": \"").append(cat.getAge()).append("\",");
            json.append("\"weight\": \"").append(cat.getWeight()).append("\",");
            json.append("\"trainingStatus\": \"").append(cat.getTrainingStatus()).append("\",");
            json.append("\"reserved\": ").append(cat.getReserved()).append(",");
            json.append("\"location\": \"").append(monitoringSystem.getAnimalLocation(cat.getName())).append("\"");
            json.append("}");
            if (i < cats.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    /*
    Function: serializeBirds
    @params: none
    Description: Converts bird list to JSON string
    */
    private String serializeBirds() {
        StringBuilder json = new StringBuilder("[");
        List<Bird> birds = animalService.getBirdList();
        for (int i = 0; i < birds.size(); i++) {
            Bird bird = birds.get(i);
            json.append("{");
            json.append("\"name\": \"").append(bird.getName()).append("\",");
            json.append("\"species\": \"").append(bird.getSpecies()).append("\",");
            json.append("\"gender\": \"").append(bird.getGender()).append("\",");
            json.append("\"age\": \"").append(bird.getAge()).append("\",");
            json.append("\"weight\": \"").append(bird.getWeight()).append("\",");
            json.append("\"trainingStatus\": \"").append(bird.getTrainingStatus()).append("\",");
            json.append("\"reserved\": ").append(bird.getReserved()).append(",");
            json.append("\"location\": \"").append(monitoringSystem.getAnimalLocation(bird.getName())).append("\"");
            json.append("}");
            if (i < birds.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    /*
    Function: serializeRabbits
    @params: none
    Description: Converts rabbit list to JSON string
    */
    private String serializeRabbits() {
        StringBuilder json = new StringBuilder("[");
        List<Rabbit> rabbits = animalService.getRabbitList();
        for (int i = 0; i < rabbits.size(); i++) {
            Rabbit rabbit = rabbits.get(i);
            json.append("{");
            json.append("\"name\": \"").append(rabbit.getName()).append("\",");
            json.append("\"breed\": \"").append(rabbit.getBreed()).append("\",");
            json.append("\"gender\": \"").append(rabbit.getGender()).append("\",");
            json.append("\"age\": \"").append(rabbit.getAge()).append("\",");
            json.append("\"weight\": \"").append(rabbit.getWeight()).append("\",");
            json.append("\"trainingStatus\": \"").append(rabbit.getTrainingStatus()).append("\",");
            json.append("\"reserved\": ").append(rabbit.getReserved()).append(",");
            json.append("\"location\": \"").append(monitoringSystem.getAnimalLocation(rabbit.getName())).append("\"");
            json.append("}");
            if (i < rabbits.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    /*
    Function: getStatsJson
    @params: none
    Description: Returns animal statistics as JSON string
    */
    private String getStatsJson() {
        int total = animalService.getTotalAnimals();
        int available = animalService.getAvailableAnimals();
        int reserved = 0;
        int training = 0;
        
        for (Dog dog : animalService.getDogList()) {
            if (dog.getReserved()) reserved++;
            else if (!"in service".equalsIgnoreCase(dog.getTrainingStatus())) training++;
        }
        for (Monkey monkey : animalService.getMonkeyList()) {
            if (monkey.getReserved()) reserved++;
            else if (!"in service".equalsIgnoreCase(monkey.getTrainingStatus())) training++;
        }
        for (Cat cat : animalService.getCatList()) {
            if (cat.getReserved()) reserved++;
            else if (!"in service".equalsIgnoreCase(cat.getTrainingStatus())) training++;
        }
        for (Bird bird : animalService.getBirdList()) {
            if (bird.getReserved()) reserved++;
            else if (!"in service".equalsIgnoreCase(bird.getTrainingStatus())) training++;
        }
        for (Rabbit rabbit : animalService.getRabbitList()) {
            if (rabbit.getReserved()) reserved++;
            else if (!"in service".equalsIgnoreCase(rabbit.getTrainingStatus())) training++;
        }
        
        return String.format("{\"total\": %d, \"available\": %d, \"reserved\": %d, \"training\": %d}",
                           total, available, reserved, training);
    }
    
    /*
    Function: addAnimalFromParams
    @params:
    params: form parameters map
    addedBy: username of person adding animal
    Description: Creates new animal from form data
    */
    private boolean addAnimalFromParams(Map<String, String> params, String addedBy) {
        String type = params.get("type");
        String name = params.get("name");
        
        if (type == null || name == null) return false;
        
        try {
            switch (type.toLowerCase()) {
                case "dog":
                    Dog dog = new Dog(
                        name,
                        params.get("breed"),
                        params.get("gender"),
                        params.get("age"),
                        params.get("weight"),
                        params.get("acquisitionDate"),
                        params.get("acquisitionCountry"),
                        params.get("trainingStatus"),
                        Boolean.parseBoolean(params.getOrDefault("reserved", "false")),
                        params.get("inServiceCountry")
                    );
                    animalService.addDog(dog, addedBy);
                    return true;
                    
                case "monkey":
                    Monkey monkey = new Monkey(
                        name,
                        params.get("gender"),
                        params.get("age"),
                        params.get("weight"),
                        params.get("species"),
                        params.get("tailLength"),
                        params.get("height"),
                        params.get("bodyLength"),
                        params.get("acquisitionDate"),
                        params.get("acquisitionCountry"),
                        params.get("trainingStatus"),
                        Boolean.parseBoolean(params.getOrDefault("reserved", "false")),
                        params.get("inServiceCountry")
                    );
                    animalService.addMonkey(monkey, addedBy);
                    return true;
                    
                case "cat":
                    Cat cat = new Cat(
                        name,
                        params.get("breed"),
                        params.get("coatColor"),
                        Boolean.parseBoolean(params.getOrDefault("declawed", "false")),
                        params.get("gender"),
                        params.get("age"),
                        params.get("weight"),
                        params.get("acquisitionDate"),
                        params.get("acquisitionCountry"),
                        params.get("trainingStatus"),
                        Boolean.parseBoolean(params.getOrDefault("reserved", "false")),
                        params.get("inServiceCountry")
                    );
                    animalService.addCat(cat, addedBy);
                    return true;
                    
                case "bird":
                    Bird bird = new Bird(
                        name,
                        params.get("species"),
                        params.get("wingspan"),
                        Boolean.parseBoolean(params.getOrDefault("canFly", "true")),
                        params.get("beakType"),
                        params.get("gender"),
                        params.get("age"),
                        params.get("weight"),
                        params.get("acquisitionDate"),
                        params.get("acquisitionCountry"),
                        params.get("trainingStatus"),
                        Boolean.parseBoolean(params.getOrDefault("reserved", "false")),
                        params.get("inServiceCountry")
                    );
                    animalService.addBird(bird, addedBy);
                    return true;
                    
                case "rabbit":
                    Rabbit rabbit = new Rabbit(
                        name,
                        params.get("breed"),
                        params.get("furColor"),
                        params.get("earType"),
                        Boolean.parseBoolean(params.getOrDefault("litterTrained", "false")),
                        params.get("gender"),
                        params.get("age"),
                        params.get("weight"),
                        params.get("acquisitionDate"),
                        params.get("acquisitionCountry"),
                        params.get("trainingStatus"),
                        Boolean.parseBoolean(params.getOrDefault("reserved", "false")),
                        params.get("inServiceCountry")
                    );
                    animalService.addRabbit(rabbit, addedBy);
                    return true;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Error adding animal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}