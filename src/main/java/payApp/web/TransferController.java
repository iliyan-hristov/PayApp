package payApp.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import payApp.transaction.model.Transaction;
import payApp.user.model.User;
import payApp.user.service.UserService;
import payApp.wallet.service.WalletService;
import payApp.web.dto.TransferRequest;

import java.util.UUID;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public TransferController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping
    public ModelAndView getTransferPage(HttpSession session){

        UUID userId = (UUID) session.getAttribute("userId");

        if (userId == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("transferRequest", new TransferRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping
    public ModelAndView transferMoney(@Valid TransferRequest transferRequest, BindingResult bindingResult, HttpSession session){


        UUID userId = (UUID) session.getAttribute("userId");

        if (userId == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);
        modelAndView.addObject("transferRequest", transferRequest);

        return modelAndView;

    }

}
