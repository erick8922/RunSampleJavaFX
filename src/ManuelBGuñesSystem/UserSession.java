package ManuelBGuñesSystem;

/**
 * Nag-iimbak ng impormasyon ng naka-login na user
 * para magamit sa ibang screens (Admin, Registrar, Student).
 */
public class UserSession {

    private static int userId;
    private static String fullName;
    private static String role;

    public static void start(int id, String name, String roleName) {
        userId = id;
        fullName = name;
        role = roleName;
    }

    public static void clear() {
        userId = 0;
        fullName = null;
        role = null;
    }

    public static int getUserId() { return userId; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }
}
