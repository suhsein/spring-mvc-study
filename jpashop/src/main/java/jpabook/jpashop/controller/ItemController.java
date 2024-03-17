package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/new")
    public String createForm(@ModelAttribute("bookForm") BookForm bookForm) {
        return "items/createItemForm";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("bookForm") BookForm bookForm) {
        Book book = new Book();
        book.setName(bookForm.getName());
        book.setPrice(bookForm.getPrice());
        book.setStockQuantity(bookForm.getStockQuantity());
        book.setAuthor(bookForm.getAuthor());
        book.setIsbn(bookForm.getIsbn());
        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/{itemId}/edit")
    public String updateItemForm(@ModelAttribute("bookForm") BookForm bookForm,
                                 @PathVariable("itemId") Long itemId) {
        Book item = (Book) itemService.findOne(itemId);
        log.info("item", item);
        bookForm.setId(itemId);
        bookForm.setName(item.getName());
        bookForm.setPrice(item.getPrice());
        bookForm.setStockQuantity(item.getStockQuantity());
        bookForm.setAuthor(item.getAuthor());
        bookForm.setIsbn(item.getIsbn());
        return "items/updateItemForm";
    }

    @PostMapping("/{itemId}/edit")
    public String updateItem(@ModelAttribute("bookForm") BookForm bookForm) {
//        Book item = (Book)itemService.findOne(bookForm.getId());
//        item.setName(bookForm.getName());
//        item.setPrice(bookForm.getPrice());
//        item.setStockQuantity(bookForm.getStockQuantity());
//        item.setAuthor(bookForm.getAuthor());
//        item.setIsbn(bookForm.getIsbn());
//
//        itemService.saveItem(item);
        itemService.updateItem(bookForm.getId(), bookForm.getName(), bookForm.getPrice(), bookForm.getStockQuantity());
        return "redirect:/items";
    }
}
