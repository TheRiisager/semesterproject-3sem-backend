package dtos;

public class UserDTO {

    private String userName, role;

    public UserDTO(String userName, String role) {
        this.userName = userName;
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }
}
