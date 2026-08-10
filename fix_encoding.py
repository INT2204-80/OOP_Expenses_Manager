import re

with open('src/main/java/expensemanager/ui/DashboardController.java', 'rb') as f:
    content = f.read().decode('ISO-8859-1') # Or just replace it as raw bytes

# Just replace the whole method body with a clean one
new_method = '''
    @FXML
    public void handleCustomPeriod() {
        javafx.scene.control.Dialog<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Ch\u1ecdn th\u1eddi gian");
        dialog.setHeaderText("Ch\u1ecdn kho\u1ea3ng th\u1eddi gian mu\u1ed1n xem");

        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("X\u00e1c nh\u1eadn", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        javafx.scene.control.DatePicker startDatePicker = new javafx.scene.control.DatePicker(currentPeriodStart);
        javafx.scene.control.DatePicker endDatePicker = new javafx.scene.control.DatePicker(currentPeriodEnd);

        grid.add(new javafx.scene.control.Label("T\u1eeb ng\u00e0y:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new javafx.scene.control.Label("\u0110\u1ebfn ng\u00e0y:"), 0, 1);
        grid.add(endDatePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new javafx.util.Pair<>(startDatePicker.getValue(), endDatePicker.getValue());
            }
            return null;
        });

        java.util.Optional<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            if (pair.getKey() != null && pair.getValue() != null) {
                if (pair.getKey().isAfter(pair.getValue())) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setHeaderText("Ng\u00e0y b\u1eaft \u0111\u1ea7u kh\u00f4ng th\u1ec3 l\u1edbn h\u01a1n ng\u00e0y k\u1ebft th\u00fac!");
                    alert.showAndWait();
                } else {
                    currentPeriodStart = pair.getKey();
                    currentPeriodEnd = pair.getValue();
                    refreshOverview();
                }
            }
        });
    }
'''

# We need to find the start of handleCustomPeriod and replace it
import io
with open('src/main/java/expensemanager/ui/DashboardController.java', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

import re
text = re.sub(r'(?s)@FXML\s+public void handleCustomPeriod\(\) \{.*?\n    \}', new_method.strip(), text)

with open('src/main/java/expensemanager/ui/DashboardController.java', 'w', encoding='utf-8') as f:
    f.write(text)

