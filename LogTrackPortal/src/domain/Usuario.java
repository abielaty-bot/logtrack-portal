package domain;

/**
 * Clase que representa un usuario del sistema LogTrack Portal.
 */
public class Usuario {
    private String nombreUsuario;
    private String contrasena;
    private Rol rol;
    private int intentosFallidos;

    /**
     * Enumeración que define los roles posibles para un usuario.
     */
    public enum Rol {
        ADMIN,
        USUARIO
    }

    /**
     * Constructor de la clase Usuario.
     *
     * @param nombreUsuario Nombre único del usuario.
     * @param contrasena    Contraseña del usuario.
     * @param rol           Rol asignado al usuario (ADMIN o USUARIO).
     */
    public Usuario(String nombreUsuario, String contrasena, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.intentosFallidos = 0;
    }

    // Getters y setters

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    /**
     * Incrementa el contador de intentos fallidos.
     */
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
    }

    /**
     * Reinicia el contador de intentos fallidos a cero.
     */
    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
    }
}
