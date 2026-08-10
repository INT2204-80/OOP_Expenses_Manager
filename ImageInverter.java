import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageInverter {
    public static void main(String[] args) throws Exception {
        File file = new File("src/main/resources/images/icon.png");
        BufferedImage image = ImageIO.read(file);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha > 0) {
                    // Make it black while keeping the same alpha
                    int blackArgb = (alpha << 24) | 0x000000;
                    image.setRGB(x, y, blackArgb);
                }
            }
        }
        ImageIO.write(image, "png", file);
    }
}
