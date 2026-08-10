import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ColorExtractor {
    public static void main(String[] args) throws Exception {
        File file = new File("src/main/resources/images/logo.png");
        BufferedImage image = ImageIO.read(file);
        Map<Integer, Integer> colors = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha > 50) {
                    int rgb = argb & 0xffffff;
                    colors.put(rgb, colors.getOrDefault(rgb, 0) + 1);
                }
            }
        }
        colors.entrySet().stream()
              .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
              .limit(10)
              .forEach(e -> {
                  System.out.printf("#%06x: %d pixels%n", e.getKey(), e.getValue());
              });
    }
}
