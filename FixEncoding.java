import java.nio.file.*;
import java.nio.charset.*;
public class FixEncoding {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/expensemanager/ui/DashboardController.java");
        byte[] bytes = Files.readAllBytes(p);
        // The file might be in some local encoding or corrupted utf-8.
        // We will just read it as ISO-8859-1 to preserve exact bytes and replace strings.
        // Actually, let's just do a string replacement in standard UTF-8.
        String content = new String(bytes, StandardCharsets.UTF_8);
        
        content = content.replace("Ch?n th?i gian", "Chọn thời gian");
        content = content.replace("Ch?n kho?ng th?i gian mu?n xem", "Chọn khoảng thời gian muốn xem");
        content = content.replace("X\uFFFDc nh?n", "Xác nhận"); // Sometimes it's a replacement char
        content = content.replace("Xc nh?n", "Xác nhận"); 
        content = content.replace("Xc nh?n", "Xác nhận"); 
        content = content.replace("T? ng\uFFFDy:", "Từ ngày:");
        content = content.replace("T? ngy:", "Từ ngày:");
        content = content.replace("T? ngy:", "Từ ngày:");
        content = content.replace("D?n ng\uFFFDy:", "Đến ngày:");
        content = content.replace("D?n ngy:", "Đến ngày:");
        content = content.replace("D?n ngy:", "Đến ngày:");
        content = content.replace("Ng\uFFFDy b?t d?u kh\uFFF0ng th? l?n hon ng\uFFFDy k?t th\uFFFDc!", "Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
        content = content.replace("Ngy b?t d?u khng th? l?n hon ngy k?t thc!", "Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
        content = content.replace("Ngy b?t d?u khng th? l?n hon ngy k?t thc!", "Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
        
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
    }
}
