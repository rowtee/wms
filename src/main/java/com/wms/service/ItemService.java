package com.wms.service;

import com.wms.dao.ItemDao;
import com.wms.model.Item;

import java.util.List;
import java.util.Optional;

/**
 * Service layer coordinating business operations for items.
 */
public class ItemService {

    private final ItemDao itemDao;

    public ItemService() {
        this.itemDao = new ItemDao();
    }

    public ItemService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public List<Item> getAllItems() {
        return itemDao.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemDao.findById(id);
    }

    public long getItemCount() {
        return itemDao.count();
    }
}
