package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.UserItemService;
import com.yifan.lightning.deal.service.model.UserItemModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/useritem")
public class UserItemController {

    @Autowired
    private UserItemService userItemService;

    @GetMapping("/list")
    public List<UserItemModel> listUserItems(Authentication authentication) {
        UserModel userModel = (UserModel) authentication.getPrincipal();
        Integer userId = userModel.getId();
        List<UserItemModel> result = userItemService.listItemByUserId(userId);
        return result;
    }

    @PostMapping("/add")
    public CommonReturnType addUserItem(@RequestParam("itemId") Integer itemId,
                                    @RequestParam("amount") Integer amount,
                                    Authentication authentication) {
        UserModel userModel = (UserModel) authentication.getPrincipal();
        Integer userId = userModel.getId();
        Integer affectedRowNumber = userItemService.addItem(userId, itemId, amount);
        if (affectedRowNumber == 1) {
            return CommonReturnType.create("Successfully added the item to the cart");
        } else {
            return CommonReturnType.create("Failed to add the item, please check the parameters or try again later", "fail");
        }
    }

    @PutMapping("/update")
    public CommonReturnType updateUserItem(@RequestParam("itemId") Integer itemId,
                                           @RequestParam("amount") Integer amount,
                                           Authentication authentication) {
        UserModel userModel = (UserModel) authentication.getPrincipal();
        Integer userId = userModel.getId();
        UserItemModel userItemModel = userItemService.selectItemByUserIdAndItemId(userId, itemId);
        userItemModel.setAmount(amount);
        Integer affectedRowNumber = userItemService.updateByPrimaryKeySelective(userItemModel);
        if (affectedRowNumber == 1) {
            return CommonReturnType.create("Successfully updated the item");
        } else {
            return CommonReturnType.create("Failed to update the item, please check the parameters or try again later", "fail");
        }
    }

    @DeleteMapping("/delete")
    public CommonReturnType deleteUserItems(@RequestBody List<Integer> itemIds,
                                            Authentication authentication) {
        UserModel userModel = (UserModel) authentication.getPrincipal();
        Integer userId = userModel.getId();
        int affectedRowNumber = userItemService.deleteItemsByUserIdAndItemId(userId, itemIds);
        if (affectedRowNumber == itemIds.size()) {
            return CommonReturnType.create("Successfully deleted the items");
        } else {
            return CommonReturnType.create("Failed to delete the items", "fail");
        }
    }

}
