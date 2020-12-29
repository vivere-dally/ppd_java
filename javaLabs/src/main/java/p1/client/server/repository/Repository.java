package p1.client.server.repository;

import lombok.NonNull;
import p1.client.server.domain.model.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * CRUD operations repository interface
 *
 * @param <ID> - type E must have an attribute of type ID
 * @param <E>  - type of entities saved in repository
 */
public interface Repository<ID extends Serializable, E extends Entity<ID>> {
    /**
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return the entity with the specified id
     * or null - if there is no entity with the given id
     * @throws NullPointerException if id is null.
     */
    E findOne(@NonNull ID id) throws NullPointerException;

    /**
     * @return all entities
     */
    List<E> findAll();

    /**
     * @param entity entity must be not null
     * @return null- if the given entity is saved
     * otherwise returns the entity (id already exists)
     * @throws NullPointerException if the given entity is null. *
     */
    E save(@NonNull E entity) throws NullPointerException;

    /**
     * @param entity entity must not be null
     * @return null - if the entity is updated,
     * otherwise returns the entity - (e.g id does not
     * exist).
     * @throws NullPointerException if the given entity is null.
     */
    E update(@NonNull E entity) throws NullPointerException;

    /**
     * removes the entity with the specified id
     *
     * @param id id must be not null
     * @return the removed entity or null if there is no entity with the
     * given id
     * @throws NullPointerException if the given id is null.
     */
    E delete(@NonNull ID id);

    /**
     * returns the number of stored entities
     */
    int size();
}