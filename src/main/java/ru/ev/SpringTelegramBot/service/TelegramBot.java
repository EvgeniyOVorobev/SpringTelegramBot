package ru.ev.SpringTelegramBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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

    static final String YES_BUTTON="YES_BUTTON";
    static final String NO_BUTTON="NO_BUTTON";
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

            if(messageText.contains("/send")&&botConfig.getOwnerId()==chatId){
                var textToSend=EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users= userRepository.findAll();
                for (User user:users){
                    prepareMessage(user.getChadId(), textToSend);
                }
            }

            else {
                switch (messageText) {
                    case "/start":
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        registerUser(update.getMessage());
                        break;
                    case "/help":
                        prepareMessage(chatId, HELP_TEXT);
                        break;

                    case "/register":
                        register(chatId);


                    default:

                        prepareMessage(chatId, "Sorry");

                }
            }
        }

        else if(update.hasCallbackQuery()){
            long messageId =update.getCallbackQuery().getMessage().getMessageId();
            long chatId=update.getCallbackQuery().getMessage().getChatId();
            String callBackData=update.getCallbackQuery().getData();
            if(callBackData.equals(YES_BUTTON)){
                String text="You pressed yes button";
                executeEditMessageText(text,chatId,messageId);
            }
            else if(callBackData.equals(NO_BUTTON)){
                String text="You pressed No button";
                executeEditMessageText(text,chatId,messageId);
            }
        }
    }

    private void executeEditMessageText(String text, long chatId, long messageId) {

        EditMessageText messageText=new EditMessageText();
        messageText.setChatId(String.valueOf(chatId));
        messageText.setText(text);
        messageText.setMessageId((int) messageId);
        try{
            execute(messageText);
        }catch (TelegramApiException e){
            log.error("Error "+e.getMessage());
        }
    }

    private void executeMessage(SendMessage message){
        try{
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error "+e.getMessage());
        }
    }

    private void register(long chatId) {
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine=new ArrayList<>();
        List<InlineKeyboardButton> rowInLine=new ArrayList<>();
        var button=new InlineKeyboardButton();
        button.setText("Yes");
        button.setCallbackData(YES_BUTTON);

        var button1=new InlineKeyboardButton();
        button1.setText("No");
        button1.setCallbackData(NO_BUTTON);

        rowInLine.add(button);
        rowInLine.add(button1);
        rowsInLine.add(rowInLine);

        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        executeMessage(message);

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
        String answer= EmojiParser.parseToUnicode("Hi, " + name + ", " + "nice to meet you"+":blush:");
        //String answer = "Hi, " + name + ", " + "nice to meet you";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows=new ArrayList<>();
        KeyboardRow row=new KeyboardRow();

        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        KeyboardRow row1=new KeyboardRow();
        row1.add("register");
        row1.add("check my data");
        row1.add("delete my data");
        keyboardRows.add(row1);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);



    }

    private void prepareMessage(long chatId,String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
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
