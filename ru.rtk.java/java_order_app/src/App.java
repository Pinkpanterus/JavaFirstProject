import java.math.BigDecimal;
import java.sql.*;

public class App {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "qwerty123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            // Пример: вставка товара
            String insertProduct = "INSERT INTO product (name, description, price, quantity, category) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProduct)) {
                ps.setString(1, "MacBook Pro");
                ps.setString(2, "Apple laptop");
                ps.setBigDecimal(3, new BigDecimal("2500.00"));
                ps.setInt(4, 5);
                ps.setString(5, "Electronics");
                ps.executeUpdate();
            }

            // Пример: чтение заказов
            String query = """
                SELECT o.id, c.first_name, p.name, o.quantity, o.order_date
                FROM "order" o
                JOIN customer c ON o.customer_id = c.id
                JOIN product p ON o.product_id = p.id
                ORDER BY o.order_date DESC
                LIMIT 5
            """;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    System.out.printf("Order #%d | Customer: %s | Product: %s | Qty: %d | Date: %s%n",
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDate("order_date"));
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}