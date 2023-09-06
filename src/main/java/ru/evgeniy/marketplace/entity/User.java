package ru.evgeniy.marketplace.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(name = "avatar_path", nullable = false)
    private String avatarPath;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Ad> ads = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void addAd(Ad ad) {
        this.ads.add(ad);
        ad.setUser(this);
    }

    public void removeAd(Ad ad) {
        this.ads.remove(ad);
        ad.setUser(null);
    }
}