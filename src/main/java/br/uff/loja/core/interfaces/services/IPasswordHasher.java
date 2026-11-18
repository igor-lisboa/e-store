package br.uff.loja.core.interfaces.services;

public interface IPasswordHasher {
    String hash(String password);

    boolean matches(String candidate, String storedValue);

    boolean isEncoded(String storedValue);
}
