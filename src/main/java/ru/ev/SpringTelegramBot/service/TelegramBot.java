package ru.ev.SpringTelegramBot.service;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ev.SpringTelegramBot.config.BotConfig;
import ru.ev.SpringTelegramBot.entity.User;
import ru.ev.SpringTelegramBot.entity.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    public UserRepository userRepository;

    private final BotConfig botConfig;
    static final String HELP_TEXT ="This bot is created to demonstrate Spring capabilities.\n\n"+
            "You can execute commands from the main menu on the left or by typing a command: \n\n "+
            "Type /start to see welcome message\n\n"+
            "Type /mydata to see data stored about yourself\n\n"+
            "Type /help to see this message again";

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> list = new ArrayList<>();
        list.add(new BotCommand("/start", "get a welcome message"));
        list.add(new BotCommand("/mydata", "get your data stored"));
        list.add(new BotCommand("/deletedata", "delete my data"));
        list.add(new BotCommand("/help", "info how to use this bot"));
        list.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(list, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();


            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:

                    sendMessage(chatId, "Sorry");

            }
        }
    }

    private void registerUser(Message message) {

        if(userRepository.findById(message.getChatId()).isEmpty()){
            var chatId=message.getChatId();
            var chat=message.getChat();
            User user=new User();
            user.setChadId(chatId);
            user.setFirsName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved "+user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", " + "nice to meet you";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

    }


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }


    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
