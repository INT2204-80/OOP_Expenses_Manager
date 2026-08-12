package expensemanager.service;

/**
 * Lỗi nghiệp vụ khi validate giao dịch (số tiền <= 0, vượt hạn mức, vượt số
 * dư ví...). Tách riêng khỏi UI để logic validate có thể unit test được mà
 * không cần khởi động JavaFX, và để controller chỉ việc bắt exception này
 * rồi hiển thị Alert với message tương ứng — không phải viết lại điều kiện
 * if/else validate ở cả dialog Thêm và dialog Sửa như file gốc.
 */
public class TransactionValidationException extends RuntimeException {
    public TransactionValidationException(String message) {
        super(message);
    }
}
