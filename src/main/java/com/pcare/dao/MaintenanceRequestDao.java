package com.pcare.dao;

import com.pcare.database.DatabaseConnection;
import com.pcare.model.MaintenanceRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRequestDao {

    // CREATE: Report a computer problem
    public boolean addRequest(MaintenanceRequest request) {

        String sql = """
                INSERT INTO Maintenance_Requests
                (computer_id, reported_by, assigned_to,
                 problem_description, date_reported, status,
                 repair_notes, date_completed)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, request.getComputerId());
            statement.setInt(2, request.getReportedBy());

            if (request.getAssignedTo() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, request.getAssignedTo());
            }

            statement.setString(4, request.getProblemDescription());
            statement.setString(5, request.getDateReported());
            statement.setString(6, request.getStatus());
            statement.setString(7, request.getRepairNotes());
            statement.setString(8, request.getDateCompleted());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding maintenance request: "
                    + e.getMessage());
            return false;
        }
    }

    // READ: View all maintenance requests
    public List<MaintenanceRequest> getAllRequests() {

        List<MaintenanceRequest> requests = new ArrayList<>();

        String sql = """
                SELECT * FROM Maintenance_Requests
                ORDER BY request_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                requests.add(mapRequest(result));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving requests: "
                    + e.getMessage());
        }

        return requests;
    }

    // READ: Find a request by ID
    public MaintenanceRequest getRequestById(int requestId) {

        String sql = """
                SELECT * FROM Maintenance_Requests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapRequest(result);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding request: " + e.getMessage());
        }

        return null;
    }

    // READ: View requests reported by a particular user
    public List<MaintenanceRequest> getRequestsByReporter(int userId) {

        List<MaintenanceRequest> requests = new ArrayList<>();

        String sql = """
                SELECT * FROM Maintenance_Requests
                WHERE reported_by = ?
                ORDER BY request_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    requests.add(mapRequest(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving reported requests: "
                    + e.getMessage());
        }

        return requests;
    }

    // READ: View requests assigned to a technician
    public List<MaintenanceRequest> getRequestsByTechnician(int technicianId) {

        List<MaintenanceRequest> requests = new ArrayList<>();

        String sql = """
                SELECT * FROM Maintenance_Requests
                WHERE assigned_to = ?
                ORDER BY request_id DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, technicianId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    requests.add(mapRequest(result));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving assigned requests: "
                    + e.getMessage());
        }

        return requests;
    }

    // UPDATE: Assign a technician to a request
    public boolean assignTechnician(int requestId, int technicianId) {

        String sql = """
                UPDATE Maintenance_Requests
                SET assigned_to = ?
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, technicianId);
            statement.setInt(2, requestId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error assigning technician: "
                    + e.getMessage());
            return false;
        }
    }

    // UPDATE: Change request status
    public boolean updateStatus(int requestId, String status) {

        if (!status.equals("Pending")
                && !status.equals("In Progress")
                && !status.equals("Completed")) {

            System.out.println("Invalid status.");
            System.out.println("Use Pending, In Progress, or Completed.");
            return false;
        }

        String sql = """
                UPDATE Maintenance_Requests
                SET status = ?,
                    date_completed = CASE
                        WHEN ? = 'Completed' THEN date('now', 'localtime')
                        ELSE NULL
                    END
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setString(2, status);
            statement.setInt(3, requestId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating request status: "
                    + e.getMessage());
            return false;
        }
    }

    // UPDATE: Record repair notes
    public boolean updateRepairNotes(int requestId, String repairNotes) {

        String sql = """
                UPDATE Maintenance_Requests
                SET repair_notes = ?
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, repairNotes);
            statement.setInt(2, requestId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating repair notes: "
                    + e.getMessage());
            return false;
        }
    }

    // UPDATE: Edit the problem description
    public boolean updateProblemDescription(int requestId, String description) {

        String sql = """
                UPDATE Maintenance_Requests
                SET problem_description = ?
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, description);
            statement.setInt(2, requestId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating problem description: "
                    + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove a maintenance request
    public boolean deleteRequest(int requestId) {

        String sql = """
                DELETE FROM Maintenance_Requests
                WHERE request_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting request: "
                    + e.getMessage());
            return false;
        }
    }

    // Convert a database row into a MaintenanceRequest object
    private MaintenanceRequest mapRequest(ResultSet result)
            throws SQLException {

        int assignedId = result.getInt("assigned_to");

        Integer assignedTo = result.wasNull() ? null : assignedId;

        return new MaintenanceRequest(
                result.getInt("request_id"),
                result.getInt("computer_id"),
                result.getInt("reported_by"),
                assignedTo,
                result.getString("problem_description"),
                result.getString("date_reported"),
                result.getString("status"),
                result.getString("repair_notes"),
                result.getString("date_completed")
        );
    }
}