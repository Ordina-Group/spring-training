package be.ordina.springtraining.ex04springtesting.controller;

import be.ordina.springtraining.ex04springtesting.model.User;
import be.ordina.springtraining.ex04springtesting.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/pages")
public class UserThymeleafController {

    private final UserService usersService;

    public UserThymeleafController(UserService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/users-overview")
    public String usersOverview(Model model) {
        final List<User> users = this.usersService.findAll();
        model.addAttribute("users", users);
        return "users-overview";
    }

    @GetMapping("/add-user")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUserForm(@ModelAttribute User user) {
        this.usersService.create(user);
        return "redirect:/pages/users-overview";
    }

}