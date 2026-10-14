package com.pcare.dao;

import com.pcare.database.DatabaseConnection;
import com.pcare.model.Computer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComputerDao {

    // CREATE: Add a computer
    public boolean addComputer(Computer computer) {

        String sql = """
                INSERT INTO Computers
                (computer_name, location, specifications, condition, date_registered)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, computer.getComputerName());
            statement.setString(2, computer.getLocation());
            statement.setString(3, computer.getSpecifications());
            statement.setString(4, computer.getCondition());
            statement.setString(5, computer.getDateRegistered());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding computer: " + e.getMessage());
            return false;
        }
    }

    // READ: View all computers
    public List<Computer> getAllComputers() {

        List<Computer> computers = new ArrayList<>();

        String sql = "SELECT * FROM Computers ORDER BY computer_id";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Computer computer = new Computer(
                        result.getInt("computer_id"),
                        result.getString("computer_name"),
                        result.getString("location"),
                        result.getString("specifications"),
                        result.getString("condition"),
                        result.getString("date_registered")
                );

                computers.add(computer);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving computers: " + e.getMessage());
        }

        return computers;
    }

    // READ: Find a computer by ID
    public Computer getComputerById(int computerId) {

        String sql = "SELECT * FROM Computers WHERE computer_id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, computerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new Computer(
                            result.getInt("computer_id"),
                            result.getString("computer_name"),
                            result.getString("location"),
                            result.getString("specifications"),
                            result.getString("condition"),
                            result.getString("date_registered")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding computer: " + e.getMessage());
        }

        return null;
    }

    // READ: Search computers by name or location
    public List<Computer> searchComputers(String keyword) {

        List<Computer> computers = new ArrayList<>();

        String sql = """
                SELECT * FROM Computers
                WHERE computer_name LIKE ? OR location LIKE ?
                ORDER BY computer_id
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";

            statement.setString(1, searchTerm);
            statement.setString(2, searchTerm);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    computers.add(new Computer(
                            result.getInt("computer_id"),
                            result.getString("computer_name"),
                            result.getString("location"),
                            result.getString("specifications"),
                            result.getString("condition"),
                            result.getString("date_registered")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching computers: " + e.getMessage());
        }

        return computers;
    }

    // UPDATE: Edit computer information
    public boolean updateComputer(Computer computer) {

        String sql = """
                UPDATE Computers
                SET computer_name = ?,
                    location = ?,
                    specifications = ?,
                    condition = ?,
                    date_registered = ?
                WHERE computer_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, computer.getComputerName());
            statement.setString(2, computer.getLocation());
            statement.setString(3, computer.getSpecifications());
            statement.setString(4, computer.getCondition());
            statement.setString(5, computer.getDateRegistered());
            statement.setInt(6, computer.getComputerId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating computer: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Change computer condition only
    public boolean updateComputerCondition(int computerId, String condition) {

        String sql = """
                UPDATE Computers
                SET condition = ?
                WHERE computer_id = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, condition);
            statement.setInt(2, computerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating computer condition: "
                    + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove a computer
    public boolean deleteComputer(int computerId) {

        String sql = "DELETE FROM Computers WHERE computer_id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, computerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Unable to delete computer. It may have maintenance requests. "
                            + e.getMessage()
            );
            return false;
        }
    }
}