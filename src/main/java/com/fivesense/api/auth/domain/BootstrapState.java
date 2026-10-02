package com.fivesense.api.auth.domain;

import jakarta.persistence.*;

@Entity @Table(name="bootstrap_state")
public class BootstrapState {
    @Id private Integer id;
    @Column(nullable=false) private boolean consumed;
    @Column(name="initial_password_ciphertext") private String initialPasswordCiphertext;
    protected BootstrapState(){}
    public boolean isConsumed(){return consumed;}
    public String getInitialPasswordCiphertext(){return initialPasswordCiphertext;}
    public void setInitialPasswordCiphertext(String ciphertext){initialPasswordCiphertext=ciphertext;}
    public void consume(){consumed=true;initialPasswordCiphertext=null;}
}
