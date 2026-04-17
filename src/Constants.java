import java.awt.*;

public class Constants {
    public static Color GREEN = new Color(0, 223, 162);
    public static Color ButtonBackground = new Color(0, 121, 255);
    public static Color ChatAreaBackground = new Color(255, 255, 255);
    public static Color WHITE = new Color(255, 255, 255);
    public static Color RED = new Color(255, 0, 96);
    public static Color BLACK = new Color(0, 0, 0);
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static Color MessageSELFBackground = new Color(0, 121, 255);
    public static Color MessageOTHERBackground = new Color(249, 181, 114);

    public static enum MessageOwner {SELF, OTHER}

    public static int MAX_MESSAGE_LENGTH = 50;
    public static int MAX_USERNAME_LENGTH = 50;
}
