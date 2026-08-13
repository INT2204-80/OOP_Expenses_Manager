// package core;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.Statement;

// public class AlterDBScript {
//     public static void main(String[] args) {
//         String url = "jdbc:mysql://localhost:3306/expense_manager";
//         String user = "root";
//         String pass = "";
        
//         try {
//             Class.forName("com.mysql.cj.jdbc.Driver");
//             try (Connection conn = DriverManager.getConnection(url, user, pass);
//                  Statement stmt = conn.createStatement()) {
                
//                 String sql = "ALTER TABLE wallets ADD COLUMN currency VARCHAR(10) DEFAULT 'VND'";
//                 stmt.executeUpdate(sql);
//                 System.out.println("Column added successfully!");
                
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
