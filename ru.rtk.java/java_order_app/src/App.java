import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;

public class App {

    private static final String PROP_FILE = "/application.properties";

    public static void main(String[] args) {
        Properties cfg = loadProps();
        String url  = cfg.getProperty("db.url");
        String user = cfg.getProperty("db.user");
        String pass = cfg.getProperty("db.password");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, pass);
            conn.setAutoCommit(false);          // транзакция

            /* 1. Вставка нового товара и покупателя */
            int newProductId = insertProduct(conn, "Xiaomi 13 Lite", "Mid-range phone",
                    new BigDecimal("499.00"), 20, "Electronics");
            int newCustId    = insertCustomer(conn, "Igor", "Sokolov",
                    "+79998887766", "igor@mail.com");
            conn.commit();

            /* 2. Создание заказа */
            int orderId = createOrder(conn, newProductId, newCustId, 2);
            conn.commit();

            /* 3. Чтение последних 5 заказов с JOIN */
            printLast5Orders(conn);

            /* 4. Обновление цены и количества товара */
            updateProductPriceAndQty(conn, newProductId,
                    new BigDecimal("479.00"), 18);
            conn.commit();

            /* 5. Удаление тестовых записей */
            deleteOrder(conn, orderId);
            deleteCustomer(conn, newCustId);
            deleteProduct(conn, newProductId);
            conn.commit();

            System.out.println("\n=== Все операции выполнены успешно ===");

        } catch (SQLException e) {
            System.err.println("Ошибка БД: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Транзакция отменена (rollback)");
                } catch (SQLException ex) {
                    System.err.println("Не удалось rollback: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    /* ---------- CRUD-методы ---------- */

    private static int insertProduct(Connection c, String name, String desc,
                                     BigDecimal price, int qty, String cat) throws SQLException {
        String sql = "INSERT INTO product (name, description, price, quantity, category) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, desc);
            ps.setBigDecimal(3, price); ps.setInt(4, qty); ps.setString(5, cat);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); int id = rs.getInt(1);
                System.out.printf("✓ Вставлен товар: id=%d  %s%n", id, name); return id;
            }
        }
    }

    private static int insertCustomer(Connection c, String fn, String ln,
                                      String phone, String email) throws SQLException {
        String sql = "INSERT INTO customer (first_name, last_name, phone, email) " +
                "VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fn); ps.setString(2, ln);
            ps.setString(3, phone); ps.setString(4, email);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); int id = rs.getInt(1);
                System.out.printf("✓ Вставлен покупатель: id=%d  %s %s%n", id, fn, ln); return id;
            }
        }
    }

    private static int createOrder(Connection c, int prodId, int custId, int qty) throws SQLException {
        String sql = "INSERT INTO \"order\" (product_id, customer_id, order_date, quantity, status_id) " +
                "VALUES (?, ?, CURRENT_DATE, ?, 1) RETURNING id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, prodId); ps.setInt(2, custId); ps.setInt(3, qty);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); int id = rs.getInt(1);
                System.out.printf("✓ Создан заказ: id=%d  товар=%d  покупатель=%d  кол-во=%d%n",
                        id, prodId, custId, qty); return id;
            }
        }
    }

    private static void printLast5Orders(Connection c) throws SQLException {
        String sql = "SELECT o.id, c.first_name, c.last_name, p.name, o.quantity, o.order_date, os.name AS status " +
                "FROM \"order\" o " +
                "JOIN customer c ON o.customer_id = c.id " +
                "JOIN product p ON o.product_id = p.id " +
                "JOIN order_status os ON o.status_id = os.id " +
                "ORDER BY o.order_date DESC, o.id DESC LIMIT 5";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("\n--- Последние 5 заказов ---");
            while (rs.next()) {
                System.out.printf("Заказ #%d | %s %s | %-20s | %d шт. | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("name"), rs.getInt("quantity"),
                        rs.getDate("order_date"), rs.getString("status"));
            }
        }
    }

    private static void updateProductPriceAndQty(Connection c, int prodId,
                                                 BigDecimal newPrice, int newQty) throws SQLException {
        String sql = "UPDATE product SET price = ?, quantity = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, newPrice); ps.setInt(2, newQty); ps.setInt(3, prodId);
            int rows = ps.executeUpdate();
            if (rows == 1) System.out.printf("✓ Обновлён товар id=%d  цена=%s  кол-во=%d%n", prodId, newPrice, newQty);
        }
    }

    private static void deleteOrder(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM \"order\" WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 1) System.out.printf("✓ Удалён заказ id=%d%n", id);
        }
    }

    private static void deleteCustomer(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM customer WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 1) System.out.printf("✓ Удалён покупатель id=%d%n", id);
        }
    }

    private static void deleteProduct(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM product WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 1) System.out.printf("✓ Удалён товар id=%d%n", id);
        }
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = App.class.getResourceAsStream(PROP_FILE)) {
            if (in == null) throw new RuntimeException("Файл " + PROP_FILE + " не найден");
            p.load(in);
        } catch (IOException e) { throw new RuntimeException(e); }
        return p;
    }
}