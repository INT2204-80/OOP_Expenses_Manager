# 💰 OOP Expenses Manager

Chào mừng bạn đến với **OOP Expenses Manager** – một ứng dụng Desktop mạnh mẽ, trực quan giúp bạn quản lý tài chính cá nhân toàn diện. Đây là sản phẩm thực hành cho bài tập lớn môn **Lập trình Hướng đối tượng (OOP) - INT2204-80** tại Trường Đại học Công nghệ (VNU-UET), được xây dựng trên nền tảng **Java 21** và **JavaFX**.

---

## ✨ Các Tính Năng Nổi Bật

### 💳 1. Quản lý Đa Tài Khoản (Multi-Wallet)
- **Đa dạng loại ví:** Hỗ trợ Ví Tiền Mặt (Cash Wallet), Tài khoản Ngân hàng (Bank Account), và Ví Điện Tử (E-Wallet).
- **Tính toán linh hoạt:** Xem tổng số dư gom từ tất cả các ví, xóa ví, đổi tên và khởi tạo số dư ban đầu.

### 📝 2. Ghi Nhận Thu Chi Chuyên Sâu (Transactions)
- **Thu & Chi:** Ghi chép chi tiết ngày tháng, số tiền, ghi chú, và phân loại danh mục (Income / Expense).
- **Tính năng Lặp lại (Recurring Expenses):** Tự động sinh các khoản chi cố định (như tiền thuê nhà, tiền mạng...) theo chu kỳ đã đặt ra (ngày, tuần, tháng, năm) và theo dõi số kỳ đã qua nhờ cơ chế Backfill tự động mỗi lần mở ví.
- **Bộ lọc mạnh mẽ (Filters):** Lọc giao dịch theo khoảng thời gian tùy chỉnh, mức tiền, danh mục, hoặc tìm kiếm theo từ khóa.

### 🏷️ 3. Tùy Biến Danh Mục Linh Hoạt (Categories)
- **Cá nhân hóa:** Cho phép tạo danh mục thu/chi mới, chọn icon và gán màu sắc riêng biệt.
- **Gộp danh mục (Merge Categories):** Gộp các danh mục trùng lặp vào một danh mục duy nhất mà vẫn bảo toàn lịch sử giao dịch.
- **Xóa mềm (Soft-delete):** Ẩn danh mục khỏi giao diện chọn nhưng vẫn giữ lại trong CSDL để không phá hỏng thống kê cũ.

### 📊 4. Thống Kê & Phân Tích (Dashboard & Analytics)
- **Biểu đồ đa dạng:** Sử dụng Area Chart (số dư), Stacked Bar Chart (thu/chi), và Pie Chart (cơ cấu danh mục).
- **Dashboard:** Cập nhật real-time Tổng thu, Tổng chi, và Dòng tiền thuần (Net Cash Flow) theo từng khoảng thời gian (Ngày/Tuần/Tháng).

### 🎯 5. Ngân Sách Thông Minh (Budgets)
- Thiết lập hạn mức chi tiêu cho từng danh mục riêng biệt trên từng ví.
- Có thanh tiến độ (Progress bar) trực quan theo dõi % chi tiêu hiện tại.
- Tự động thay đổi cảnh báo (chuyển đỏ) khi vượt quá hạn mức ngân sách.

---

## 🛠️ Kiến Trúc & Công Nghệ

Dự án áp dụng chặt chẽ các nguyên lý Thiết kế Hướng đối tượng (OOP) và kiến trúc N-Tier đa tầng:

- **Kế Thừa & Đa Hình (Inheritance & Polymorphism):** 
  - Class trừu tượng `Wallet` được mở rộng ra thành `CashWallet`, `BankAccountWallet`, và `EWallet`.
  - Class `Transaction` được kế thừa bởi `Income`, `Expense` và `RecurringExpense`.
- **N-Tier Architecture & DAO Pattern:** 
  - **Data Access Object (DAO):** Phân tách hoàn toàn logic truy xuất cơ sở dữ liệu với các giao thức chuẩn (Interface: `ITransactionDAO`, `ICategoryDAO`, `IWalletDAO`, `IBudgetDAO`).
  - **Service Layer:** Xử lý toàn bộ logic nghiệp vụ trung gian (`TransactionService`, `CategoryService`, `BudgetService`), các thao tác logic đều được thực hiện ở tầng này, sau đó Service gọi xuống DAO.
  - **UI / Controller Layer:** Giao diện JavaFX chỉ gọi đến Service Layer để xử lý, áp dụng Dependency Injection (DI) để đưa Service vào Controller.

**Stack Công nghệ sử dụng:**
- **Ngôn ngữ:** Java 21
- **UI Framework:** JavaFX 21.0.1
- **Database:** MySQL 8.3 (Kết nối qua JDBC)
- **Build Tool:** Maven
- **Kiểm thử:** JUnit 6 (Jupiter)

---

## 🚀 Hướng Dẫn Cài Đặt và Khởi Chạy

### 1. Chuẩn bị Môi Trường
- Đã cài đặt **JDK 21** trở lên.
- Đã cài đặt **Maven** (3.6+).
- Đã cài đặt **MySQL Server**.

### 2. Thiết lập Database (MySQL)
1. Mở MySQL Workbench (hoặc Terminal) và tạo một database mới:
   ```sql
   CREATE DATABASE expense_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Chạy file script SQL được cung cấp sẵn tại đường dẫn `src/main/resources/database/schema.sql` để khởi tạo các bảng.
3. Mở file `src/main/java/core/storage/DatabaseConnection.java` và cấu hình biến `USER` và `PASSWORD` cho khớp với môi trường của bạn.

### 3. Build và Chạy Ứng Dụng
Mở Terminal / Command Prompt tại thư mục gốc của dự án và chạy:

```bash
# Biên dịch dự án
mvn clean compile

# Chạy ứng dụng qua JavaFX plugin
mvn javafx:run
```

*Hoặc bạn có thể mở dự án bằng **IntelliJ IDEA**, chờ load Maven và chạy class `expensemanager.Launcher`.*

---

## 👨‍💻 Tác Giả & Bản Quyền
- Dự án thuộc môn học **Lập trình Hướng đối tượng (INT2204-80)** - Trường Đại học Công nghệ, ĐHQGHN.
- Mã nguồn mở nhằm mục đích học tập và tham khảo.