package core.storage;

import java.io.IOException;
import java.util.List;

import core.transaction.Transaction;

public interface Storage {

    /**
     * Lưu danh sách giao dịch xuống file
     * @param transactions Danh sách các giao dịch cần lưu
     * @param path Đường dẫn tới file (ví dụ: "data.csv" hoặc "data.json")
     * @throws IOException Bắt buộc các class cài đặt phải xử lý lỗi nạp/ghi file
     */
    void save(List<Transaction> transactions, String path) throws IOException;

    /**
     * Nạp danh sách giao dịch từ file lên bộ nhớ
     * @param path Đường dẫn tới file cần đọc
     * @return Danh sách các giao dịch đã được nạp
     * @throws IOException Bắt buộc các class cài đặt phải xử lý lỗi nạp/ghi file
     */
    List<Transaction> load(String path) throws IOException;

}