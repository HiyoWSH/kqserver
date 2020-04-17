package kq.server.controller;

import kq.server.service.CardService;
import kq.server.util.CardShop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class KqSchedulController {

    @Autowired
    private CardService cardService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void kqSchedule(){
        CardShop.setShopCards(cardService.createShopCards());
    }
}
