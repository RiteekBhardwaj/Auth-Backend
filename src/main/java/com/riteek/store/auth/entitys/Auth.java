package com.riteek.store.auth.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.riteek.store.auth.types.Providers;
import com.riteek.store.auth.types.Roles;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@Table(
        name = "user_auth",
        indexes = {
                @Index(name = "idx_auth_role", columnList = "role"),
                @Index(name = "idx_auth_provider", columnList = "provider")
        }
)
public class Auth {

    public Auth(String email, String passwordHash, Roles role, Providers provider){
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
        this.setRole(role);
        this.setProvider(provider);
        this.setTwoFactorEnabled(false);
    }
    public Auth(){}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @ToString.Exclude
    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Enumerated(EnumType.STRING)
    private Providers provider;

    @Column(nullable = false)
    private boolean twoFactorEnabled=false;

    private String twoFactorCredential;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    //domain methods
    public void changePassword(String newPasswordHash){
        this.passwordHash=newPasswordHash;
    }

    public void changeLastLogin(){
        this.lastLogin=LocalDateTime.now();
    }
    
    public void enableTwoFactor(String credential){
        this.twoFactorEnabled=true;
        this.twoFactorCredential=credential;
    }

    public void disableTwoFactor(){
        this.twoFactorEnabled=false;
        this.twoFactorCredential=null;
    }
}
