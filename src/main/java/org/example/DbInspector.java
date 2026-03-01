package org.example;

import org.example.utils.DatabaseConnection;
import java.sql.*;

public class DbInspector {
    public static void main(String[] args) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.out.println("Could not connect to database.");
                return;
            }
            DatabaseMetaData meta = con.getMetaData();

            System.out.println("--- Table: reservation ---");
            try (ResultSet rs = meta.getColumns(null, null, "reservation", null)) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    String type = rs.getString("TYPE_NAME");
                    int size = rs.getInt("COLUMN_SIZE");
                    String nullable = rs.getString("IS_NULLABLE");
                    System.out.println(name + " | " + type + "(" + size + ") | Nullable: " + nullable);
                }
            }

            System.out.println("\n--- Foreign Keys for reservation ---");
            try (ResultSet rs = meta.getImportedKeys(null, null, "reservation")) {
                while (rs.next()) {
                    String pkTable = rs.getString("PKTABLE_NAME");
                    String pkCol = rs.getString("PKCOLUMN_NAME");
                    String fkCol = rs.getString("FKCOLUMN_NAME");
                    System.out.println("FK " + fkCol + " -> " + pkTable + "(" + pkCol + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
