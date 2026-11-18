package br.uff.store.core.dtos;

import java.util.Map;

public class UserDTO extends BaseDTO {

    private Integer id;
    private String name;
    private String email;
    private transient String password;
    private Integer roleId;

    public UserDTO() {
    }

    public UserDTO(
            Integer id,
            String name,
            String email,
            String password,
            Integer roleId
    ) {
        this.setId(id);
        this.setName(name);
        this.setEmail(email);
        this.setPassword(password);
        this.setRoleId(roleId);
    }

    public UserDTO(Map<String, Object> user) {
        this.setId(Integer.valueOf(String.valueOf(user.get("id"))));
        this.setName(String.valueOf(user.get("name")));
        this.setEmail(String.valueOf(user.get("email")));
        this.setPassword(String.valueOf(user.get("password")));
        this.setRoleId(Integer.valueOf(String.valueOf(user.get("roleId"))));
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getPassword() {
        return password;
    }

    public Integer getId() {
        return id;
    }
}
