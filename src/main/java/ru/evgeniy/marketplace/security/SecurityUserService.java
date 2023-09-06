package ru.evgeniy.marketplace.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.evgeniy.marketplace.entity.Role;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SecurityUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public SecurityUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optional = getWrappedUser(username);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }
        User user = optional.get();
        Collection<? extends GrantedAuthority> authorities = authorities(user.getRoles());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    private Collection<? extends GrantedAuthority> authorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    public Optional<User> getWrappedUser(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}