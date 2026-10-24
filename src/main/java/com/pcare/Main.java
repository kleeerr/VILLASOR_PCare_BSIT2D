package com.pcare;

import com.pcare.dao.DatabaseInitializer;
import com.pcare.dao.ActivityLogDao;
import com.pcare.dao.ComputerDao;
import com.pcare.dao.MaintenanceRequestDao;
import com.pcare.dao.UserDao;
import com.pcare.model.ActivityLog;
import com.pcare.model.Computer;
import com.pcare.model.MaintenanceRequest;
import com.pcare.model.User;
import com.pcare.service.AuthenticationService;
import com.pcare.ui.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    // ==================== INITIALIZATION ====================

    private static final Scanner scanner = new Scanner(System.in);

    private static final DatabaseInitializer databaseInitializer =
            new DatabaseInitializer();

    private static final AuthenticationService authService =
            new AuthenticationService();

    private static final UserDao userDao = new UserDao();
    private static final ComputerDao computerDao = new ComputerDao();
    private static final MaintenanceRequestDao requestDao =
            new MaintenanceRequestDao();
    private static final ActivityLogDao activityLogDao =
            new ActivityLogDao();

    private static final Authentication authentication =
            new Authentication(userDao, authService, scanner);

    private static User loggedInUser;

    // ==================== MAIN ====================

    public static void main(String[] args) {

        try {
            databaseInitializer.initialize();

            while (true) {
                System.out.println("\n=================================================");
                System.out.println(" PCare Computer Laboratory Maintenance Tracker");
                System.out.println("=================================================");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");

                int choice = readInt("Choose an option: ");

                switch (choice) {
                    case 1 -> login();
                    case 2 -> authentication.register();
                    case 3 -> {
                        System.out.println("Thank you for using PCare!");
                        return;
                    }
                    default -> System.out.println(
                            "Invalid choice. Please try again."
                    );
                }
            }

        } catch (Exception e) {
            System.out.println("Unable to start PCare.");
            System.out.println("Error: " + e.getMessage());

        } finally {
            scanner.close();
        }
    }

    // ==================== LOGIN ====================

    private static void login() {

        User user = authentication.login();

        if (user == null) {
            return;
        }

        loggedInUser = user;

        activityLogDao.addLog(
                loggedInUser.getUserId(),
                "Logged in"
        );

        try {
            switch (loggedInUser.getRole()) {
                case "Administrator" -> administratorDashboard();
                case "Technician" -> technicianDashboard();
                case "Laboratory Assistant" ->
                        laboratoryAssistantDashboard();
                default -> System.out.println("Invalid user role.");
            }

        } finally {
            if (loggedInUser != null) {
                activityLogDao.addLog(
                        loggedInUser.getUserId(),
                        "Logged out"
                );

                loggedInUser = null;
            }
        }
    }

    // ==================== ADMINISTRATOR DASHBOARD ====================

    private static void administratorDashboard() {

        while (loggedInUser != null) {

            System.out.println("\n========== ADMINISTRATOR DASHBOARD ==========");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Computers");
            System.out.println("3. Manage Maintenance Requests");
            System.out.println("4. Generate Summary Reports");
            System.out.println("5. View Activity Logs");
            System.out.println("6. Logout");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> manageUsers();
                case 2 -> manageComputers();
                case 3 -> manageMaintenanceRequests();
                case 4 -> generateReports();
                case 5 -> viewActivityLogs();
                case 6 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ==================== USER MANAGEMENT ====================

    private static void manageUsers() {

        while (true) {

            System.out.println("\n========== MANAGE USERS ==========");
            System.out.println("1. Add User");
            System.out.println("2. View All Users");
            System.out.println("3. Search User by ID");
            System.out.println("4. Update User");
            System.out.println("5. Activate/Deactivate User");
            System.out.println("6. Delete User");
            System.out.println("7. Back");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> addUser();
                case 2 -> viewAllUsers();
                case 3 -> searchUser();
                case 4 -> updateUser();
                case 5 -> changeUserStatus();
                case 6 -> deleteUser();
                case 7 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addUser() {

        System.out.println("\n========== ADD USER ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.println("\nAvailable Roles:");
        System.out.println("1. Administrator");
        System.out.println("2. Technician");
        System.out.println("3. Laboratory Assistant");

        int roleChoice = readInt("Choose a role: ");

        String role;

        switch (roleChoice) {
            case 1 -> role = "Administrator";
            case 2 -> role = "Technician";
            case 3 -> role = "Laboratory Assistant";
            default -> {
                System.out.println("Invalid role.");
                return;
            }
        }

        if (username.isBlank() || password.isBlank()
                || fullName.isBlank()) {
            System.out.println("All fields are required.");
            return;
        }

        User user = new User(username, password, fullName, role);

        if (userDao.addUser(user)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Added user: " + username
            );

            System.out.println("User added successfully!");
        } else {
            System.out.println(
                    "Failed to add user. The username may already exist."
            );
        }
    }

    private static void viewAllUsers() {

        System.out.println("\n========== ALL USERS ==========");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.printf(
                "%-8s %-18s %-25s %-25s %-12s%n",
                "ID", "Username", "Full Name", "Role", "Status"
        );

        System.out.println(
                "--------------------------------------------------------------------------------------"
        );

        for (User user : users) {
            System.out.printf(
                    "%-8d %-18s %-25s %-25s %-12s%n",
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole(),
                    user.getStatus()
            );
        }
    }

    private static void searchUser() {

        int id = readPositiveId("Enter User ID: ");

        if (id == -1) {
            return;
        }

        User user = userDao.getUserById(id);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        displayUser(user);
    }

    private static void displayUser(User user) {

        System.out.println("\nUser ID: " + user.getUserId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Full Name: " + user.getFullName());
        System.out.println("Role: " + user.getRole());
        System.out.println("Status: " + user.getStatus());
    }

    private static void updateUser() {

        int id = readPositiveId("Enter User ID to update: ");

        if (id == -1) {
            return;
        }

        User user = userDao.getUserById(id);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        if (loggedInUser.getUserId() == id) {
            System.out.println(
                    "You cannot update your own account through this menu."
            );
            return;
        }

        System.out.println("Leave a field blank to keep its current value.");

        System.out.print("New Username (" + user.getUsername() + "): ");
        String username = scanner.nextLine().trim();

        System.out.print("New Full Name (" + user.getFullName() + "): ");
        String fullName = scanner.nextLine().trim();

        System.out.print("New Password: ");
        String password = scanner.nextLine();

        System.out.println("\nCurrent Role: " + user.getRole());
        System.out.println("1. Keep Current Role");
        System.out.println("2. Administrator");
        System.out.println("3. Technician");
        System.out.println("4. Laboratory Assistant");

        int roleChoice = readInt("Choose an option: ");

        if (!username.isBlank()) {
            user.setUsername(username);
        }

        if (!fullName.isBlank()) {
            user.setFullName(fullName);
        }

        if (!password.isBlank()) {
            user.setPassword(password);
        }

        switch (roleChoice) {
            case 1 -> {
                // Keep current role.
            }
            case 2 -> user.setRole("Administrator");
            case 3 -> user.setRole("Technician");
            case 4 -> user.setRole("Laboratory Assistant");
            default -> {
                System.out.println("Invalid role option.");
                return;
            }
        }

        if (userDao.updateUser(user)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated user ID: " + id
            );

            System.out.println("User updated successfully!");
        } else {
            System.out.println(
                    "Failed to update user. Check whether the username already exists."
            );
        }
    }

    private static void changeUserStatus() {

        int id = readPositiveId("Enter User ID: ");

        if (id == -1) {
            return;
        }

        User user = userDao.getUserById(id);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        if (loggedInUser.getUserId() == id) {
            System.out.println("You cannot deactivate your own account.");
            return;
        }

        System.out.println("Current status: " + user.getStatus());
        System.out.println("1. Activate");
        System.out.println("2. Deactivate");

        int choice = readInt("Choose an option: ");

        String status;

        switch (choice) {
            case 1 -> status = "Active";
            case 2 -> status = "Inactive";
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        if (userDao.updateUserStatus(id, status)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    status + " user ID: " + id
            );

            System.out.println("User status updated successfully!");
        } else {
            System.out.println("Failed to update user status.");
        }
    }

    private static void deleteUser() {

        int id = readPositiveId("Enter User ID to delete: ");

        if (id == -1) {
            return;
        }

        if (loggedInUser.getUserId() == id) {
            System.out.println("You cannot delete your own account.");
            return;
        }

        User user = userDao.getUserById(id);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("User: " + user.getFullName());
        System.out.println(
                "Warning: deletion may fail if this user has related records."
        );

        if (!confirm("Are you sure you want to delete this user?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (userDao.deleteUser(id)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Deleted user ID: " + id
            );

            System.out.println("User deleted successfully!");
        } else {
            System.out.println(
                    "Could not delete the user. They may be referenced by maintenance requests or activity logs."
            );
        }
    }

    // ==================== COMPUTER MANAGEMENT ====================

    private static void manageComputers() {

        while (true) {

            System.out.println("\n========== MANAGE COMPUTERS ==========");
            System.out.println("1. Add Computer");
            System.out.println("2. View All Computers");
            System.out.println("3. Search Computers");
            System.out.println("4. Update Computer");
            System.out.println("5. Update Computer Condition");
            System.out.println("6. Delete Computer");
            System.out.println("7. Back");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> addComputer();
                case 2 -> viewAllComputers();
                case 3 -> searchComputers();
                case 4 -> updateComputer();
                case 5 -> updateComputerCondition();
                case 6 -> deleteComputer();
                case 7 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addComputer() {

        System.out.println("\n========== ADD COMPUTER ==========");

        System.out.print("Computer Name/Asset Label: ");
        String name = scanner.nextLine().trim();

        System.out.print("Laboratory/Location: ");
        String location = scanner.nextLine().trim();

        System.out.print("Specifications: ");
        String specifications = scanner.nextLine().trim();

        System.out.println("\nCondition:");
        System.out.println("1. Good");
        System.out.println("2. Needs Maintenance");
        System.out.println("3. Under Repair");

        String condition = readCondition();

        if (name.isBlank() || location.isBlank()) {
            System.out.println("Computer name and location are required.");
            return;
        }

        Computer computer = new Computer(
                name,
                location,
                specifications,
                condition,
                LocalDate.now().toString()
        );

        if (computerDao.addComputer(computer)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Added computer: " + name
            );

            System.out.println("Computer added successfully!");
        } else {
            System.out.println("Failed to add computer.");
        }
    }

    private static void viewAllComputers() {

        System.out.println("\n========== COMPUTER INVENTORY ==========");

        List<Computer> computers = computerDao.getAllComputers();

        if (computers.isEmpty()) {
            System.out.println("No computers registered.");
            return;
        }

        displayComputers(computers);
    }

    private static void displayComputers(List<Computer> computers) {

        System.out.printf(
                "%-6s %-22s %-20s %-24s %-20s %-12s%n",
                "ID", "Computer", "Location", "Specifications",
                "Condition", "Date"
        );

        System.out.println(
                "-------------------------------------------------------------------------------------------------------------------"
        );

        for (Computer computer : computers) {
            System.out.printf(
                    "%-6d %-22s %-20s %-24s %-20s %-12s%n",
                    computer.getComputerId(),
                    shorten(computer.getComputerName(), 21),
                    shorten(computer.getLocation(), 19),
                    shorten(
                            computer.getSpecifications() == null
                                    ? ""
                                    : computer.getSpecifications(),
                            23
                    ),
                    computer.getCondition(),
                    computer.getDateRegistered()
            );
        }
    }

    private static void searchComputers() {

        System.out.print("Enter computer name or location: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isBlank()) {
            System.out.println("Search keyword is required.");
            return;
        }

        List<Computer> results = computerDao.searchComputers(keyword);

        if (results.isEmpty()) {
            System.out.println("No matching computers found.");
        } else {
            displayComputers(results);
        }
    }

    private static void updateComputer() {

        int id = readPositiveId("Enter Computer ID to update: ");

        if (id == -1) {
            return;
        }

        Computer computer = computerDao.getComputerById(id);

        if (computer == null) {
            System.out.println("Computer not found.");
            return;
        }

        System.out.println("Leave a field blank to keep its current value.");

        System.out.print("Computer Name (" + computer.getComputerName() + "): ");
        String name = scanner.nextLine().trim();

        System.out.print("Location (" + computer.getLocation() + "): ");
        String location = scanner.nextLine().trim();

        System.out.print(
                "Specifications (" + computer.getSpecifications() + "): "
        );
        String specifications = scanner.nextLine().trim();

        if (!name.isBlank()) {
            computer.setComputerName(name);
        }

        if (!location.isBlank()) {
            computer.setLocation(location);
        }

        if (!specifications.isBlank()) {
            computer.setSpecifications(specifications);
        }

        if (computerDao.updateComputer(computer)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated computer ID: " + id
            );

            System.out.println("Computer updated successfully!");
        } else {
            System.out.println("Failed to update computer.");
        }
    }

    private static void updateComputerCondition() {

        int id = readPositiveId("Enter Computer ID: ");

        if (id == -1) {
            return;
        }

        Computer computer = computerDao.getComputerById(id);

        if (computer == null) {
            System.out.println("Computer not found.");
            return;
        }

        System.out.println("Current condition: " + computer.getCondition());
        System.out.println("1. Good");
        System.out.println("2. Needs Maintenance");
        System.out.println("3. Under Repair");

        String condition = readCondition();

        if (computerDao.updateComputerCondition(id, condition)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated condition of computer ID: " + id
            );

            System.out.println("Computer condition updated!");
        } else {
            System.out.println("Failed to update computer condition.");
        }
    }

    private static void deleteComputer() {

        int id = readPositiveId("Enter Computer ID to delete: ");

        if (id == -1) {
            return;
        }

        Computer computer = computerDao.getComputerById(id);

        if (computer == null) {
            System.out.println("Computer not found.");
            return;
        }

        if (!confirm("Delete " + computer.getComputerName() + "?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (computerDao.deleteComputer(id)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Deleted computer ID: " + id
            );

            System.out.println("Computer deleted successfully!");
        } else {
            System.out.println(
                    "Could not delete this computer. It may have related maintenance requests."
            );
        }
    }

    // ==================== MAINTENANCE REQUEST MANAGEMENT ====================

    private static void manageMaintenanceRequests() {

        while (true) {

            System.out.println("\n========== MAINTENANCE REQUESTS ==========");
            System.out.println("1. View All Requests");
            System.out.println("2. View Request by ID");
            System.out.println("3. Assign Technician");
            System.out.println("4. Update Request Status");
            System.out.println("5. Update Repair Notes");
            System.out.println("6. Update Problem Description");
            System.out.println("7. Delete Request");
            System.out.println("8. Back");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> viewAllRequests();
                case 2 -> viewRequestById();
                case 3 -> assignTechnician();
                case 4 -> updateRequestStatus();
                case 5 -> updateRepairNotes();
                case 6 -> updateProblemDescription();
                case 7 -> deleteRequest();
                case 8 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAllRequests() {

        System.out.println("\n========== ALL MAINTENANCE REQUESTS ==========");

        List<MaintenanceRequest> requests = requestDao.getAllRequests();

        if (requests.isEmpty()) {
            System.out.println("No maintenance requests found.");
            return;
        }

        displayRequests(requests);
    }

    private static void displayRequests(List<MaintenanceRequest> requests) {

        System.out.printf(
                "%-6s %-9s %-10s %-12s %-15s %-12s %-24s%n",
                "ID", "Computer", "Reporter", "Technician",
                "Status", "Reported", "Problem"
        );

        System.out.println(
                "-----------------------------------------------------------------------------------------------"
        );

        for (MaintenanceRequest request : requests) {
            String assigned = request.getAssignedTo() == null
                    ? "Unassigned"
                    : String.valueOf(request.getAssignedTo());

            System.out.printf(
                    "%-6d %-9d %-10d %-12s %-15s %-12s %-24s%n",
                    request.getRequestId(),
                    request.getComputerId(),
                    request.getReportedBy(),
                    assigned,
                    request.getStatus(),
                    request.getDateReported(),
                    shorten(request.getProblemDescription(), 23)
            );
        }
    }

    private static void viewRequestById() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null) {
            System.out.println("Maintenance request not found.");
            return;
        }

        displayRequestDetails(request);
    }

    private static void displayRequestDetails(MaintenanceRequest request) {

        System.out.println("\n========== REQUEST DETAILS ==========");
        System.out.println("Request ID: " + request.getRequestId());
        System.out.println("Computer ID: " + request.getComputerId());
        System.out.println("Reported By User ID: " + request.getReportedBy());

        System.out.println(
                "Assigned Technician ID: "
                        + (request.getAssignedTo() == null
                        ? "Unassigned"
                        : request.getAssignedTo())
        );

        System.out.println("Problem: " + request.getProblemDescription());
        System.out.println("Date Reported: " + request.getDateReported());
        System.out.println("Status: " + request.getStatus());
        System.out.println("Repair Notes: " + request.getRepairNotes());
        System.out.println("Date Completed: " + request.getDateCompleted());
    }

    private static void assignTechnician() {

        int requestId = readPositiveId("Enter Request ID: ");

        if (requestId == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(requestId);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        int technicianId = readPositiveId("Enter Technician User ID: ");

        if (technicianId == -1) {
            return;
        }

        User technician = userDao.getUserById(technicianId);

        if (technician == null
                || !"Technician".equals(technician.getRole())
                || !"Active".equals(technician.getStatus())) {
            System.out.println(
                    "The selected user is not an active technician."
            );
            return;
        }

        if (requestDao.assignTechnician(requestId, technicianId)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Assigned technician " + technicianId
                            + " to request " + requestId
            );

            System.out.println("Technician assigned successfully!");
        } else {
            System.out.println("Failed to assign technician.");
        }
    }

    private static void updateRequestStatus() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        System.out.println("Current status: " + request.getStatus());
        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");

        int choice = readInt("Choose the new status: ");

        String status;

        switch (choice) {
            case 1 -> status = "Pending";
            case 2 -> status = "In Progress";
            case 3 -> status = "Completed";
            default -> {
                System.out.println("Invalid status.");
                return;
            }
        }

        if (requestDao.updateStatus(id, status)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Changed request " + id + " status to " + status
            );

            System.out.println("Request status updated!");
        } else {
            System.out.println("Failed to update request status.");
        }
    }

    private static void updateRepairNotes() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        System.out.print("Enter repair notes: ");
        String notes = scanner.nextLine().trim();

        if (notes.isBlank()) {
            System.out.println("Repair notes cannot be empty.");
            return;
        }

        if (requestDao.updateRepairNotes(id, notes)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated repair notes for request " + id
            );

            System.out.println("Repair notes saved!");
        } else {
            System.out.println("Failed to save repair notes.");
        }
    }

    private static void updateProblemDescription() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        System.out.print("Enter updated problem description: ");
        String description = scanner.nextLine().trim();

        if (description.isBlank()) {
            System.out.println("Description cannot be empty.");
            return;
        }

        if (requestDao.updateProblemDescription(id, description)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated problem description for request " + id
            );

            System.out.println("Problem description updated!");
        } else {
            System.out.println("Failed to update description.");
        }
    }

    private static void deleteRequest() {

        int id = readPositiveId("Enter Request ID to delete: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null) {
            System.out.println("Request not found.");
            return;
        }

        if (!confirm("Are you sure you want to delete this request?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (requestDao.deleteRequest(id)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Deleted maintenance request " + id
            );

            System.out.println("Request deleted successfully!");
        } else {
            System.out.println("Failed to delete request.");
        }
    }

    // ==================== REPORTS ====================

    private static void generateReports() {

        List<Computer> computers = computerDao.getAllComputers();
        List<MaintenanceRequest> requests = requestDao.getAllRequests();
        List<User> users = userDao.getAllUsers();

        int good = 0;
        int needsMaintenance = 0;
        int underRepair = 0;

        for (Computer computer : computers) {
            switch (computer.getCondition()) {
                case "Good" -> good++;
                case "Needs Maintenance" -> needsMaintenance++;
                case "Under Repair" -> underRepair++;
            }
        }

        int pending = 0;
        int inProgress = 0;
        int completed = 0;

        for (MaintenanceRequest request : requests) {
            switch (request.getStatus()) {
                case "Pending" -> pending++;
                case "In Progress" -> inProgress++;
                case "Completed" -> completed++;
            }
        }

        int administrators = 0;
        int technicians = 0;
        int assistants = 0;

        for (User user : users) {
            switch (user.getRole()) {
                case "Administrator" -> administrators++;
                case "Technician" -> technicians++;
                case "Laboratory Assistant" -> assistants++;
            }
        }

        System.out.println("\n========== PCare SUMMARY REPORT ==========");
        System.out.println("Generated: " + LocalDate.now());

        System.out.println("\n--- Computer Inventory ---");
        System.out.println("Total Computers: " + computers.size());
        System.out.println("Good Condition: " + good);
        System.out.println("Needs Maintenance: " + needsMaintenance);
        System.out.println("Under Repair: " + underRepair);

        System.out.println("\n--- Maintenance Requests ---");
        System.out.println("Total Requests: " + requests.size());
        System.out.println("Pending: " + pending);
        System.out.println("In Progress: " + inProgress);
        System.out.println("Completed: " + completed);

        System.out.println("\n--- User Summary ---");
        System.out.println("Total Users: " + users.size());
        System.out.println("Administrators: " + administrators);
        System.out.println("Technicians: " + technicians);
        System.out.println("Laboratory Assistants: " + assistants);

        activityLogDao.addLog(
                loggedInUser.getUserId(),
                "Generated summary report"
        );
    }

    // ==================== ACTIVITY LOGS ====================

    private static void viewActivityLogs() {

        System.out.println("\n========== ACTIVITY LOGS ==========");

        List<ActivityLog> logs = activityLogDao.getAllLogs();

        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        System.out.printf(
                "%-8s %-10s %-35s %-22s%n",
                "Log ID", "User ID", "Action", "Date and Time"
        );

        System.out.println(
                "--------------------------------------------------------------------------"
        );

        for (ActivityLog log : logs) {
            System.out.printf(
                    "%-8d %-10d %-35s %-22s%n",
                    log.getLogId(),
                    log.getUserId(),
                    shorten(log.getAction(), 34),
                    log.getDateTime()
            );
        }
    }

    // ==================== TECHNICIAN DASHBOARD ====================

    private static void technicianDashboard() {

        while (loggedInUser != null) {

            System.out.println("\n========== TECHNICIAN DASHBOARD ==========");
            System.out.println("1. View Assigned Requests");
            System.out.println("2. View Request Details");
            System.out.println("3. Update Repair Status");
            System.out.println("4. Record Repair Notes");
            System.out.println("5. Logout");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> viewAssignedRequests();
                case 2 -> viewAssignedRequestDetails();
                case 3 -> updateAssignedRequestStatus();
                case 4 -> updateAssignedRepairNotes();
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAssignedRequests() {

        List<MaintenanceRequest> requests =
                requestDao.getRequestsByTechnician(
                        loggedInUser.getUserId()
                );

        System.out.println("\n========== MY ASSIGNED REQUESTS ==========");

        if (requests.isEmpty()) {
            System.out.println("You have no assigned requests.");
            return;
        }

        displayRequests(requests);
    }

    private static MaintenanceRequest getAssignedRequest(int requestId) {

        MaintenanceRequest request = requestDao.getRequestById(requestId);

        if (request == null) {
            System.out.println("Request not found.");
            return null;
        }

        if (request.getAssignedTo() == null
                || request.getAssignedTo() != loggedInUser.getUserId()) {
            System.out.println(
                    "You can only access requests assigned to you."
            );
            return null;
        }

        return request;
    }

    private static void viewAssignedRequestDetails() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = getAssignedRequest(id);

        if (request != null) {
            displayRequestDetails(request);
        }
    }

    private static void updateAssignedRequestStatus() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = getAssignedRequest(id);

        if (request == null) {
            return;
        }

        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");

        int choice = readInt("Choose the new status: ");

        String status;

        switch (choice) {
            case 1 -> status = "Pending";
            case 2 -> status = "In Progress";
            case 3 -> status = "Completed";
            default -> {
                System.out.println("Invalid status.");
                return;
            }
        }

        if (requestDao.updateStatus(id, status)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Updated assigned request " + id + " to " + status
            );

            System.out.println("Request status updated!");
        } else {
            System.out.println("Failed to update request status.");
        }
    }

    private static void updateAssignedRepairNotes() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = getAssignedRequest(id);

        if (request == null) {
            return;
        }

        System.out.print("Enter repair notes: ");
        String notes = scanner.nextLine().trim();

        if (notes.isBlank()) {
            System.out.println("Repair notes cannot be empty.");
            return;
        }

        if (requestDao.updateRepairNotes(id, notes)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Recorded repair notes for request " + id
            );

            System.out.println("Repair notes saved!");
        } else {
            System.out.println("Failed to save repair notes.");
        }
    }

    // ==================== LABORATORY ASSISTANT DASHBOARD ====================

    private static void laboratoryAssistantDashboard() {

        while (loggedInUser != null) {

            System.out.println(
                    "\n========== LABORATORY ASSISTANT DASHBOARD =========="
            );
            System.out.println("1. View Computers");
            System.out.println("2. Search Computers");
            System.out.println("3. Report Computer Problem");
            System.out.println("4. View My Reported Requests");
            System.out.println("5. View My Request Details");
            System.out.println("6. Logout");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> viewAllComputers();
                case 2 -> searchComputers();
                case 3 -> reportComputerProblem();
                case 4 -> viewMyReportedRequests();
                case 5 -> viewMyReportedRequestDetails();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void reportComputerProblem() {

        System.out.println("\n========== REPORT COMPUTER PROBLEM ==========");

        List<Computer> computers = computerDao.getAllComputers();

        if (computers.isEmpty()) {
            System.out.println(
                    "No computers are registered. Contact the administrator."
            );
            return;
        }

        displayComputers(computers);

        int computerId = readPositiveId("Enter Computer ID: ");

        if (computerId == -1) {
            return;
        }

        Computer computer = computerDao.getComputerById(computerId);

        if (computer == null) {
            System.out.println("Computer not found.");
            return;
        }

        System.out.print("Describe the problem: ");
        String description = scanner.nextLine().trim();

        if (description.isBlank()) {
            System.out.println("Problem description is required.");
            return;
        }

        MaintenanceRequest request = new MaintenanceRequest(
                computerId,
                loggedInUser.getUserId(),
                description,
                LocalDate.now().toString()
        );

        if (requestDao.addRequest(request)) {
            activityLogDao.addLog(
                    loggedInUser.getUserId(),
                    "Reported a problem for computer ID: " + computerId
            );

            System.out.println("Problem reported successfully!");
            System.out.println(
                    "The request is now pending administrator assignment."
            );
        } else {
            System.out.println("Failed to submit the maintenance request.");
        }
    }

    private static void viewMyReportedRequests() {

        List<MaintenanceRequest> requests =
                requestDao.getRequestsByReporter(
                        loggedInUser.getUserId()
                );

        System.out.println("\n========== MY REPORTED REQUESTS ==========");

        if (requests.isEmpty()) {
            System.out.println("You have not reported any problems.");
            return;
        }

        displayRequests(requests);
    }

    private static void viewMyReportedRequestDetails() {

        int id = readPositiveId("Enter Request ID: ");

        if (id == -1) {
            return;
        }

        MaintenanceRequest request = requestDao.getRequestById(id);

        if (request == null
                || request.getReportedBy() != loggedInUser.getUserId()) {
            System.out.println(
                    "Request not found among your reported requests."
            );
            return;
        }

        displayRequestDetails(request);
    }

    // ==================== INPUT HELPERS ====================

    private static int readInt(String prompt) {

        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int readPositiveId(String prompt) {

        int id = readInt(prompt);

        if (id <= 0) {
            System.out.println("Please enter a valid positive ID.");
            return -1;
        }

        return id;
    }

    private static boolean confirm(String prompt) {

        System.out.print(prompt + " (Y/N): ");
        String answer = scanner.nextLine().trim();

        return answer.equalsIgnoreCase("Y");
    }

    private static String readCondition() {

        int choice = readInt("Choose condition: ");

        return switch (choice) {
            case 1 -> "Good";
            case 2 -> "Needs Maintenance";
            case 3 -> "Under Repair";
            default -> {
                System.out.println(
                        "Invalid condition. Defaulting to Good."
                );
                yield "Good";
            }
        };
    }

    private static String shorten(String text, int maxLength) {

        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }
}