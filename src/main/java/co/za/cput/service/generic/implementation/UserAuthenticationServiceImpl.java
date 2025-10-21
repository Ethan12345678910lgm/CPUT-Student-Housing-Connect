package co.za.cput.service.generic.implementation;

import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.repository.generic.UserAuthenticationRepository;
import co.za.cput.service.generic.IUserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserAuthenticationServiceImpl implements IUserAuthenticationService {

    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    public UserAuthenticationServiceImpl(UserAuthenticationRepository userAuthenticationRepository) {
        this.userAuthenticationRepository = userAuthenticationRepository;
    }

    @Override
    public UserAuthentication create(UserAuthentication userAuthentication) {
        return userAuthenticationRepository.save(userAuthentication);
    }

    @Override
    public UserAuthentication read(Long Id) {
        return userAuthenticationRepository.findById(Id).orElse(null);
    }

    @Override
    public UserAuthentication update(UserAuthentication userAuthentication) {
        return userAuthenticationRepository.save(userAuthentication);
    }

    @Override
    public List<UserAuthentication> getAllUserAuthentications() {
        return userAuthenticationRepository.findAll();
    }

    @Override
    public void delete(Long Id) {
        if (Id == null) {
            return;
        }

        if (!userAuthenticationRepository.existsById(Id)) {
            return;
        }

        userAuthenticationRepository.deleteById(Id);
        userAuthenticationRepository.flush();
    }

    @Override
    public boolean existsByUsernameOrEmail(String usernameOrEmail) {
        Optional<String> normalized = normalize(usernameOrEmail);
        return normalized.filter(value -> userAuthenticationRepository
                        .existsByUsernameIgnoreCaseOrContact_EmailIgnoreCase(value, value))
                .isPresent();
    }

    @Override
    public Optional<UserAuthentication> findByUsernameOrEmail(String usernameOrEmail) {
        return normalize(usernameOrEmail)
                .flatMap(value -> userAuthenticationRepository
                        .findByUsernameIgnoreCaseOrContact_EmailIgnoreCase(value, value));
    }

    private Optional<String> normalize(String usernameOrEmail) {
        if (usernameOrEmail == null) {
            return Optional.empty();
        }

        String trimmed = usernameOrEmail.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(trimmed.toLowerCase(Locale.ROOT));
    }
}