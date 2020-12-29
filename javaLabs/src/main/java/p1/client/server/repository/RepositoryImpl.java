package p1.client.server.repository;

import lombok.NonNull;
import p1.client.server.domain.model.Entity;
import p1.client.server.utils.MyFileWriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RepositoryImpl<ID extends Serializable, E extends Entity<ID>> implements Repository<ID, E> {
    private final Map<ID, E> entities;

    public RepositoryImpl() {
        entities = new HashMap<>();
    }

    @Override
    public E findOne(@NonNull ID id) throws NullPointerException {
        return entities.get(id);
    }

    @Override
    public List<E> findAll() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public E save(@NonNull E entity) throws NullPointerException {
        E savedEntity = this.findOne(entity.getId());
        if (savedEntity != null) {
            return savedEntity;
        }

        this.entities.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public E update(@NonNull E entity) throws NullPointerException {
        E savedEntity = this.findOne(entity.getId());
        if (savedEntity == null) {
            throw new IllegalArgumentException("Cannot update an entity that is not present in the repository!");
        }

        this.delete(entity.getId());
        this.save(entity);
        return savedEntity;
    }

    @Override
    public E delete(@NonNull ID id) {
        E savedEntity = this.findOne(id);
        if (savedEntity == null) {
            throw new IllegalArgumentException("Cannot delete an entity that is not present in the repository!");
        }

        this.entities.remove(id);
        return savedEntity;
    }

    @Override
    public int size() {
        return this.entities.size();
    }

    public void saveToFile(String filePath) {
        MyFileWriter myFileWriter = new MyFileWriter(filePath);
        for (Map.Entry<ID, E> entry : this.entities.entrySet()) {
            myFileWriter.writeAppend(String.format("[%s] %s", entry.getKey(), entry.getValue()) + System.lineSeparator());
        }
    }
}
