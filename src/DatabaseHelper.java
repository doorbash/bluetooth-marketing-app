import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseHelper {
    Statement stmt;

    public DatabaseHelper(String dbname) {
        try {
            File f = new File(dbname);
            if (!f.isFile()) {
                f.createNewFile();
                Class.forName("org.sqlite.JDBC");

                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);

                stmt = conn.createStatement();
                stmt.execute("CREATE TABLE main.addresses (address VARCHAR NOT NULL)");

            } else {
                Class.forName("org.sqlite.JDBC");

                Connection conn = DriverManager.getConnection("jdbc:sqlite:"
                        + dbname);

                stmt = conn.createStatement();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean contains(String address) {
        try {
            ResultSet rs = stmt.executeQuery("select * from addresses where address = \'" + address + "\'");
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int Count() {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM addresses");
            int c = 0;
            try {
                while (rs.next()) {
                    c++;
                }
            } catch (Exception e) {
            }

            return c;
        } catch (Exception e) {
        }
        return -1;
    }

    public void add(String address) {
        try {
            stmt.execute("INSERT INTO addresses VALUES(\'" + address + "\')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Reset() {
        try {
            int c = Count();
            stmt.execute("DELETE FROM addresses");
            System.out.println("Reset() : " + "deleted " + c + " row(s) from database.");

        } catch (Exception e) {
            System.out.println("Not reset");
        }
    }
}
