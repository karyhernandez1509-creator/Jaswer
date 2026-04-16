package modelo;

public final class SesionUsuario {

    private static String usuario;
    private static boolean administrador;

    private SesionUsuario() {
    }

    public static void iniciarSesion(String usuarioActual, boolean esAdministrador) {
        usuario = usuarioActual;
        administrador = esAdministrador;
    }

    public static void cerrarSesion() {
        usuario = null;
        administrador = false;
    }

    public static boolean haySesionActiva() {
        return usuario != null && !usuario.trim().isEmpty();
    }

    public static String getUsuario() {
        return usuario == null ? "" : usuario;
    }

    public static boolean esAdministrador() {
        return administrador;
    }
}
