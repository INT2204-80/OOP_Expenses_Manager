# 💰 OOP Expenses Manager

Chào mừng bạn đến với **OOP Expenses Manager** – một ứng dụng Desktop mạnh mẽ, trực quan giúp bạn quản lý tài chính cá nhân toàn diện. Đây là sản phẩm thực hành cho bài tập lớn môn **Lập trình Hướng đối tượng (OOP) - INT2204-80** tại Trường Đại học Công nghệ (VNU-UET), được xây dựng trên nền tảng **Java 21** và **JavaFX**.

Thay vì chỉ là một bài tập môn học khô khan, phần mềm được trau chuốt về giao diện (UI/UX) và tích hợp các tính năng thực tế không thua kém gì các ứng dụng quản lý chi tiêu chuyên nghiệp hiện nay!

---

## ✨ Các Tính Năng Nổi Bật

Hệ thống được thiết kế bao quát hầu hết các nhu cầu quản lý tài chính khắt khe nhất:

### 💳 1. Quản lý Đa Tài Khoản (Multi-Wallet)
- **Đa dạng loại ví:**
  - **Ví Tiền Mặt (Cash Wallet):** Theo dõi lượng tiền mặt đang có.
  - **Tài khoản Ngân hàng (Bank Account):** Gắn với tên ngân hàng và số tài khoản cụ thể.
  - **Ví Điện Tử (E-Wallet):** Lưu trữ thông tin nhà cung cấp (MoMo, ZaloPay, Viettel Money...).
- **Hỗ trợ tiền tệ:** Khởi tạo ví với các loại tiền tệ khác nhau (VND, USD).
- **Tính năng mở rộng:** Xem tổng số dư gom từ tất cả các ví, xóa ví (kèm cảnh báo an toàn tránh mất dữ liệu).

### 📝 2. Ghi Nhận Thu Chi Chuyên Sâu (Transactions)
- **Giao dịch Thu & Chi:** Ghi chép chi tiết ngày tháng, số tiền, ghi chú, và phân loại danh mục một cách nhanh chóng.
- **Tính năng Lặp lại (Recurring Expenses):** Tự động xử lý các khoản chi cố định (như tiền thuê nhà, tiền mạng, Spotify, Netflix...) theo chu kỳ: *Hàng ngày, Hàng tuần, Hàng tháng, Hàng năm*.
- **Kiểm soát chi tiêu (Validation):** Ứng dụng thông minh tự động cảnh báo và ngăn chặn ngay nếu bạn cố gắng nhập khoản chi vượt quá số dư thực tế đang có trong ví.
- **Bộ lọc mạnh mẽ (Filters):** Dễ dàng tìm kiếm lại giao dịch cũ nhờ bộ lọc linh hoạt theo: Khoảng thời gian tự chọn, Mức tiền (Từ... Đến...), Danh mục, hoặc tìm kiếm theo từ khóa trong Ghi chú.

### 🏷️ 3. Tùy Biến Danh Mục Linh Hoạt (Categories)
- **Cá nhân hóa tối đa:** Cho phép tự tạo danh mục thu/chi mới, đặt tên, chọn icon sinh động (từ kho emoji) và gán màu sắc hiển thị riêng biệt.
- **Gộp danh mục (Merge Categories):** Tính năng nâng cao cực kỳ hữu ích giúp bạn dọn dẹp các danh mục trùng lặp. Bạn có thể chọn nhiều danh mục và gộp (merge) chúng vào chung một danh mục đích mà vẫn bảo toàn lịch sử giao dịch cũ.
- **Quản lý an toàn:** Cơ chế Soft-delete (xóa mềm) danh mục giúp đảm bảo các báo cáo cũ không bị lỗi khi bạn xóa một danh mục không còn dùng tới.

### 📊 4. Thống Kê & Phân Tích (Dashboard & Analytics)
- **Biểu đồ số dư (Area Chart):** Theo dõi xu hướng thay đổi tổng tài sản theo chu kỳ (Ngày/Tuần/Tháng).
- **Biểu đồ biến động (Stacked Bar Chart):** So sánh trực quan lượng tiền vào/ra qua các thời kỳ.
- **Phân bổ dòng tiền (Pie Charts):** Cung cấp 2 biểu đồ tròn riêng biệt để "mổ xẻ" chi tiết cấu trúc dòng tiền Thu và dòng tiền Chi theo từng danh mục.
- **Bảng Dashboard tổng quan:** Cập nhật real-time Tổng thu, Tổng chi, và Dòng tiền thuần (Net Cash Flow) trong bất kỳ khoảng thời gian nào bạn đang xem.

### 🎯 5. Ngân Sách Thông Minh (Budgets)
- Thiết lập hạn mức chi tiêu cho từng danh mục (Ví dụ: Ăn uống 3 triệu/tháng).
- Có thanh tiến độ (Progress bar) trực quan theo dõi % chi tiêu hiện tại.
- Tự động đổi màu cảnh báo (đỏ) khi số tiền chi thực tế tiến sát hoặc vượt quá hạn mức bạn đã đặt ra.

---

## 🛠️ Kiến Trúc & Công Nghệ

Sức mạnh thực sự của phần mềm nằm ở phần "lõi" bên dưới, tuân thủ cực kỳ chặt chẽ các nguyên lý Thiết kế Hướng đối tượng (OOP):

- **Kế Thừa & Đa Hình (Inheritance & Polymorphism):** 
  - Class trừu tượng `Wallet` được mở rộng ra thành `CashWallet`, `BankAccount`, và `EWallet`.
  - Class `Transaction` được kế thừa bởi `Income`, `Expense` và loại giao dịch phức tạp hơn là `RecurringExpense`.
- **Design Patterns:**
  - **Strategy Pattern:** Cơ chế lưu trữ được thiết kế dạng "plug-and-play", cho phép linh hoạt chuyển đổi giữa việc lưu vào MySQL (`DatabaseStorage`) hoặc xuất ra File CSV (`CsvStorage`).
  - **DAO Pattern:** Tách bạch hoàn toàn logic truy xuất cơ sở dữ liệu (`WalletDAO`, `TransactionDAO`...) khỏi logic tính toán.
  - **MVC Architecture:** Kiến trúc phân tầng rõ ràng giữa Dữ liệu (Core Models), Giao diện hiển thị (FXML/CSS) và Điều khiển (Java Controllers).

**Stack Công nghệ sử dụng:**
- **Ngôn ngữ:** Java 21
- **UI Framework:** JavaFX 21.0.1
- **Database:** MySQL 8.3
- **Build Tool:** Maven
- **Kiểm thử:** JUnit 6 (Jupiter)

---

## 🚀 Hướng Dẫn Cài Đặt và Khởi Chạy

### 1. Chuẩn bị Môi Trường
- Đã cài đặt **JDK 21** trở lên.
- Đã cài đặt **Maven** (3.6+).
- Đã cài đặt **MySQL Server** (để chạy ứng dụng với Database).

### 2. Thiết lập Database (MySQL)
1. Mở MySQL Workbench (hoặc Terminal) và tạo một database mới:
   ```sql
   CREATE DATABASE expense_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Chạy file script SQL được cung cấp sẵn tại đường dẫn `src/main/resources/database/schema.sql` để khởi tạo các bảng (wallets, categories, transactions...).
3. Mở file `src/main/java/core/storage/DatabaseConnection.java` và sửa lại `USER` / `PASSWORD` cho khớp với tài khoản MySQL của bạn.

### 3. Build và Chạy Ứng Dụng
Mở Terminal / Command Prompt tại thư mục gốc của dự án và chạy lần lượt 2 lệnh sau:

```bash
# Tải dependencies và biên dịch dự án
mvn clean compile

# Chạy ứng dụng qua JavaFX plugin
mvn javafx:run
```

*Mẹo: Bạn hoàn toàn có thể mở dự án bằng các IDE hiện đại như **IntelliJ IDEA** hoặc **VS Code**, chờ IDE nhận diện Maven dependencies, rồi chạy trực tiếp file `expensemanager.Launcher` hoặc `Main.java`.*

---

## 👨‍💻 Tác Giả & Bản Quyền

- Đây là dự án thuộc môn học **Lập trình Hướng đối tượng (INT2204-80)** - Trường Đại học Công nghệ, ĐHQGHN.
- Mã nguồn mở hoàn toàn để chia sẻ, học hỏi và tham khảo.
- Nếu bạn thấy dự án hữu ích hoặc học được gì đó từ source code, đừng ngần ngại tặng dự án một ⭐️ **Star** nhé!