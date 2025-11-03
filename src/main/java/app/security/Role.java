package app.security;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @Column(name = "rolename", nullable = false)
    private String roleName;

    public Role(String roleName) {
        this.roleName = roleName.toUpperCase();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_user",
            joinColumns = @JoinColumn(name = "rolename"),
            inverseJoinColumns = @JoinColumn(name = "username"))
    private java.util.Set<User> users = new HashSet<>();

    public void addUser(User user) {
        users.add(user);
    }
}
