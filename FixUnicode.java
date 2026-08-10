import java.nio.file.*;
import java.nio.charset.*;

public class FixUnicode {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/expensemanager/ui/DashboardController.java");
        String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);

        // Find the start and end of handleCustomPeriod
        int startIdx = content.indexOf("public void handleCustomPeriod()");
        if (startIdx == -1) {
            System.out.println("Not found");
            return;
        }
        int endIdx = content.indexOf("private void refreshOverview()", startIdx);
        if (endIdx == -1) {
            System.out.println("End not found");
            return;
        }

        // We replace everything from @FXML before handleCustomPeriod up to refreshOverview
        int fxmlIdx = content.lastIndexOf("@FXML", startIdx);

        String newMethod = "@FXML\n" +
            "    public void handleCustomPeriod() {\n" +
            "        javafx.scene.control.Dialog<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> dialog = new javafx.scene.control.Dialog<>();\n" +
            "        dialog.setTitle(\"Ch\u1ecdn th\u1eddi gian\");\n" +
            "        dialog.setHeaderText(\"Ch\u1ecdn kho\u1ea3ng th\u1eddi gian mu\u1ed1n xem\");\n" +
            "\n" +
            "        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType(\"X\u00e1c nh\u1eadn\", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);\n" +
            "        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);\n" +
            "\n" +
            "        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();\n" +
            "        grid.setHgap(10);\n" +
            "        grid.setVgap(10);\n" +
            "        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));\n" +
            "\n" +
            "        javafx.scene.control.DatePicker startDatePicker = new javafx.scene.control.DatePicker(currentPeriodStart);\n" +
            "        javafx.scene.control.DatePicker endDatePicker = new javafx.scene.control.DatePicker(currentPeriodEnd);\n" +
            "\n" +
            "        grid.add(new javafx.scene.control.Label(\"T\u1eeb ng\u00e0y:\"), 0, 0);\n" +
            "        grid.add(startDatePicker, 1, 0);\n" +
            "        grid.add(new javafx.scene.control.Label(\"\\u0110\u1ebfn ng\u00e0y:\"), 0, 1);\n" +
            "        grid.add(endDatePicker, 1, 1);\n" +
            "\n" +
            "        dialog.getDialogPane().setContent(grid);\n" +
            "\n" +
            "        dialog.setResultConverter(dialogButton -> {\n" +
            "            if (dialogButton == okButtonType) {\n" +
            "                return new javafx.util.Pair<>(startDatePicker.getValue(), endDatePicker.getValue());\n" +
            "            }\n" +
            "            return null;\n" +
            "        });\n" +
            "\n" +
            "        java.util.Optional<javafx.util.Pair<java.time.LocalDate, java.time.LocalDate>> result = dialog.showAndWait();\n" +
            "        result.ifPresent(pair -> {\n" +
            "            if (pair.getKey() != null && pair.getValue() != null) {\n" +
            "                if (pair.getKey().isAfter(pair.getValue())) {\n" +
            "                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);\n" +
            "                    alert.setHeaderText(\"Ng\u00e0y b\u1eaft \\u0111\u1ea7u kh\u00f4ng th\u1ec3 l\u1edbn h\u01a1n ng\u00e0y k\u1ebft th\u00fac!\");\n" +
            "                    alert.showAndWait();\n" +
            "                } else {\n" +
            "                    currentPeriodStart = pair.getKey();\n" +
            "                    currentPeriodEnd = pair.getValue();\n" +
            "                    refreshOverview();\n" +
            "                }\n" +
            "            }\n" +
            "        });\n" +
            "    }\n\n    ";

        String finalContent = content.substring(0, fxmlIdx) + newMethod + content.substring(endIdx);
        
        Files.write(p, finalContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("Fixed");
    }
}
