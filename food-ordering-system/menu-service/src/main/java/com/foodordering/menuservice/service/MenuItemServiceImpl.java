package com.foodordering.menuservice.service;

import com.foodordering.menuservice.dto.MenuItemRequest;
import com.foodordering.menuservice.dto.MenuItemResponse;
import com.foodordering.menuservice.exception.ResourceNotFoundException;
import com.foodordering.menuservice.model.MenuItem;
import com.foodordering.menuservice.repository.MenuItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Override
    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MenuItemResponse getMenuItemById(Long id) {
        return toResponse(findMenuItemById(id));
    }

    @Override
    public MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest) {
        MenuItem menuItem = MenuItem.builder()
                .itemName(menuItemRequest.getItemName())
                .category(menuItemRequest.getCategory())
                .price(menuItemRequest.getPrice())
                .available(menuItemRequest.getAvailable())
                .build();

        return toResponse(menuItemRepository.save(menuItem));
    }

    @Override
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest) {
        MenuItem menuItem = findMenuItemById(id);
        menuItem.setItemName(menuItemRequest.getItemName());
        menuItem.setCategory(menuItemRequest.getCategory());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setAvailable(menuItemRequest.getAvailable());

        return toResponse(menuItemRepository.save(menuItem));
    }

    @Override
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = findMenuItemById(id);
        menuItemRepository.delete(menuItem);
    }

    private MenuItem findMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id " + id));
    }

    private MenuItemResponse toResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .itemName(menuItem.getItemName())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .available(menuItem.getAvailable())
                .build();
    }
}
