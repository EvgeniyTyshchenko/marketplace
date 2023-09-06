package ru.evgeniy.marketplace.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.evgeniy.marketplace.entity.Role;
import ru.evgeniy.marketplace.constants.RoleType;
import ru.evgeniy.marketplace.service.RoleService;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;
import ru.evgeniy.marketplace.repository.RoleRepository;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRole(String name) {
        String preparedRole = RoleType.getPreparedRole(name);
        log.info("Запрос на получение роли!");
        return roleRepository.findByName(preparedRole).orElseThrow(()
                -> new EntityNotFound(Role.class, HttpStatus.BAD_REQUEST));
    }
}