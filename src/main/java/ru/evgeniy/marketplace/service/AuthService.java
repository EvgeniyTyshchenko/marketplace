package ru.evgeniy.marketplace.service;

import ru.evgeniy.marketplace.dto.other.Credentials;

public interface AuthService {

    void login(Credentials credentials);

    void register(Credentials credentials);
}