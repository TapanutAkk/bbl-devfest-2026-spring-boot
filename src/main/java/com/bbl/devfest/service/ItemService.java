package com.bbl.devfest.service;

import com.bbl.devfest.model.Item;
import com.bbl.devfest.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item %d not found".formatted(id)));
    }

    public Item create(String name) {
        return repository.save(new Item(name));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Item %d not found".formatted(id));
        }
        repository.deleteById(id);
    }
}
