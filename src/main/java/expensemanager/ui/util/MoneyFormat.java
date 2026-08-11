package expensemanager.ui.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormat {

    private MoneyFormat() {}

    /**
     * Định dạng số tiền giữ nguyên format cũ: "1,234.56 VND"
     */
    public static String format(double amount) {
        return String.format(Locale.US, "%,.2f VND", amount);
    }

    /**
     * Định dạng tiền Việt Nam chuẩn
     * Ví dụ: 1500000 -> "1.500.000 VNĐ"
     */
    public static String formatVND(double amount) {
        // Cách 1: Dùng Locale.of() (Chuẩn từ Java 19 trở đi)
        Locale vietnameseLocale = Locale.of("vi", "VN");
        

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(vietnameseLocale);
        symbols.setGroupingSeparator('.');
        
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(amount) + " VNĐ";
    }
}