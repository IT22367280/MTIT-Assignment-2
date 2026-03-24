package com.foodordering.menuservice.service;

import com.foodordering.menuservice.dto.MenuItemRequest;
import com.foodordering.menuservice.dto.MenuItemResponse;
import java.util.List;

public interface MenuItemService {

    List<MenuItemResponse> getAllMenuItems();

    MenuItemResponse getMenuItemById(Long id);

    MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest);

    MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest);

    void deleteMenuItem(Long id);
}
