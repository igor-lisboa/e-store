package br.uff.store.core.exceptions;

public final class ResourceNotFoundException extends StoreException {
    public ResourceNotFoundException(String resource) {
        super(resource + " was not found.");
    }
}
