package uz.pdp.bot;

import com.google.gson.Gson;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyBotService {
    static List<TgUser> tgUsers = new ArrayList<>();
    static List<Product> productList = new ArrayList<>(
            Arrays.asList(
                    new Product(1, "Choynak", "Xitoy Choynaki", 200D, "D:\\All\\Other\\Karosa\\Choynak.jpg"),
                    new Product(2, "Piyola", "Xitoy piyola", 100D, "D:\\All\\Other\\Karosa\\Piyola.jpg")
            )
    );

    public static SendMessage getStart(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        if (user.getBotState().equals(BotState.CHOOSE_LANG)) {


            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Til tanlang : \n Choose lang : ");
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
            markup.setResizeKeyboard(true);
            markup.setSelective(true);
            List<KeyboardRow> keyboardRowList = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton();
            button1.setText("\uD83C\uDDFA\uD83C\uDDFF  O'zbek tili");
            row.add(button1);
            KeyboardButton button2 = new KeyboardButton();
            button2.setText("\uD83C\uDDEC\uD83C\uDDE7  English language");
            row.add(button2);
            keyboardRowList.add(row);
            markup.setKeyboard(keyboardRowList);
            sendMessage.setReplyMarkup(markup);
            return sendMessage;
        } else {
            return showMenu(update);
        }
    }

    public static TgUser getUserFromListByChatId(long chatId) {
        for (TgUser user : tgUsers) {
            if (user.getChatId() == chatId) {
                return user;
            }
        }
        TgUser tgUser = new TgUser();
        tgUser.setChatId(chatId);
        tgUser.setBotState(BotState.CHOOSE_LANG);
        tgUsers.add(tgUser);
        return tgUser;
    }

    public static long getChatId(Update update) {
        return update.getCallbackQuery() != null ?
                update.getCallbackQuery().getMessage().getChatId()
                : update.getMessage().getChatId();
    }

    public static SendMessage getLang(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        String text = update.getMessage().getText();
        if (text.contains("tili")) {
            user.setLang("UZ");
        } else {
            user.setLang("EN");
        }
        user.setBotState(BotState.SHARE_CONTACT);
        user = changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ? "Telefon raqamingizni \"JO'NATISH\" tugmasini bosish orqali jo'nating , iltimos!"
                : "Please , share your phone number by pressing \"SHARE\" button");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText(user.getLang().equals("UZ") ? "JO'NATISH" : "SHARE");
        button.setRequestContact(true);
        row.add(button);
        rowList.add(row);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public static TgUser changeUserInfo(TgUser changedUser) {
        for (TgUser tgUser : tgUsers) {
            if (tgUser.getChatId() == changedUser.getChatId()) {
                tgUser = changedUser;
                return tgUser;
            }
        }
        return null;
    }

    public static SendMessage getContact(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        Contact contact = update.getMessage().getContact();
        System.out.println("PHONE NUMBER : " + contact.getPhoneNumber());
        user.setPhoneNumber(contact.getPhoneNumber().startsWith("+") ?
                contact.getPhoneNumber()
                : "+" + contact.getPhoneNumber());
        user.setBotState(BotState.SHARE_LOCATION);
        user = changeUserInfo(user);
        SendMessage sendMessage = new SendMessage().setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ? "Iltimos , Locationizni \"Location Jo'natish\" tugmasi orqali yuboring yoki addressizni yozib yuboring."
                : "Please , send your location by pressing \"Share Loacation\" button or write  your address and send it.");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText(user.getLang().equals("UZ") ? "Location Jo'natish"
                : "Share Loacation");
        button.setRequestLocation(true);
        row.add(button);
        rowList.add(row);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public static SendMessage getLocation(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        if (update.getMessage().getLocation() != null) {
            Location location = update.getMessage().getLocation();
            user.setLon(location.getLongitude());
            user.setLat(location.getLatitude());
        } else {
            user.setAddress(update.getMessage().getText());
        }
        user.setBotState(BotState.SHARE_NAME);
        user = changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ?
                "Sizning ismingiz " + update.getMessage().getFrom().getFirstName() + " ?"
                : "Is your name " + update.getMessage().getFrom().getFirstName() + " ?");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText(user.getLang().equals("UZ") ? "YO'Q" : "NO");
        row.add(button1);
        KeyboardButton button2 = new KeyboardButton();
        button2.setText(user.getLang().equals("UZ") ? "HA" : "YES");
        row.add(button2);
        keyboardRowList.add(row);
        markup.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public static SendMessage getNameByButton(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setName(update.getMessage().getFrom().getFirstName());
        user.setBotState(BotState.SHOW_MENU);
        user = changeUserInfo(user);
        return showMenu(update);
    }

    public static SendMessage askEnterName(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setBotState(BotState.ENTER_NAME);
        user = changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ?
                "Ismingizni kiriting, iltimos!!!"
                : "Enter your name , please!!!");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
        return sendMessage;
    }

    public static SendMessage getNameByEnter(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setName(update.getMessage().getText());
        user.setBotState(BotState.SHOW_MENU);
        user = changeUserInfo(user);
        return showMenu(update);
    }

    public static SendMessage showMenu(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ?
                "Menu tanlang : "
                : "Choose menu : ");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Mahsulotlar"
                : "Products");
        button.setCallbackData(BotState.SEE_PRODUCT);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Valyutalar kursini korish"
                : "See currency rate");
        button.setCallbackData(BotState.GET_CURRENCY_RATE);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Tahrirlash"
                : "Edit");
        button.setCallbackData(BotState.EDIT);
        row.add(button);

        rowList.add(row);
        markup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public static SendMessage showProduct(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setBotState(BotState.SEE_PRODUCT);
        changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ?
                "Mahsulot tanlang : "
                : "Choose product");
        return sendMessage;
    }

    public static SendMessage getProduct(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        String data = update.getCallbackQuery().getData();
        int productId = Integer.parseInt(data.substring(10));
        user.setSelectedProductId(productId);
        changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("ProductID = " + data.substring(10));
        return sendMessage;
    }

    public static SendMessage getUSDRate(Update update) throws IOException {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setBotState(BotState.GET_CURRENCY_RATE);
        changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        URL url = new URL("https://cbu.uz/oz/arkhiv-kursov-valyut/json/USD/2021-08-12/");
        URLConnection connection = url.openConnection();
        BufferedReader reader= new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String str;
        String str2="";
        while ((str=reader.readLine())!=null){
            str2+=str;
        }
        System.out.println(str2);
        Gson gson = new Gson();
        Currency[] currencies=gson.fromJson(str2,Currency[].class);
        sendMessage.setText(currencies[0].getRate());
        return sendMessage;
    }

    public static SendMessage showEditMenu(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        user.setBotState(BotState.EDIT);
        changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ") ?
                "Nimani edit qimoqchisiz ? "
                : "What do you want to change ? ");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Ism"
                : "Name");
        button.setCallbackData(BotState.CHANGE_NAME);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Telefon raqam"
                : "PhoneNumber");
        button.setCallbackData(BotState.CHANGE_PHONE);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(user.getLang().equals("UZ") ?
                "Location"
                : "Location");
        button.setCallbackData(BotState.CHANGE_LOCATION);
        row.add(button);

        rowList.add(row);
        markup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public static SendMessage change(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        String data = update.getCallbackQuery().getData();
        SendMessage sendMessage1=new SendMessage();
        sendMessage1.setChatId(chatId);

        if (data.equals(BotState.CHANGE_NAME)){
            user.setBotState(BotState.CHANGE_NAME);
            changeUserInfo(user);
            sendMessage1.setText(user.getLang().equals("UZ")?
                    "Yangi ismingizni kiriting , iltimos"
                    :"Please , enter your new name");
        }
        else if (data.equals(BotState.CHANGE_PHONE)){
            user.setBotState(BotState.CHANGE_PHONE);
            changeUserInfo(user);
            sendMessage1.setText(user.getLang().equals("UZ")?
                    "Telefon raqamingizni kiriting"
                    :"Enter your phone number");
        }
        else {
            user.setBotState(BotState.CHANGE_LOCATION);
            changeUserInfo(user);
            SendMessage sendMessage = new SendMessage().setChatId(chatId);
            sendMessage.setText(user.getLang().equals("UZ") ? "Iltimos , Locationizni \"Location Jo'natish\" tugmasi orqali yuboring yoki addressizni yozib yuboring."
                    : "Please , send your location by pressing \"Share Loacation\" button or write  your address and send it.");
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
            List<KeyboardRow> rowList = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton();
            button.setText(user.getLang().equals("UZ") ? "Location Jo'natish"
                    : "Share Loacation");
            button.setRequestLocation(true);
            row.add(button);
            rowList.add(row);
            markup.setKeyboard(rowList);
            markup.setSelective(true);
            markup.setResizeKeyboard(true);
            sendMessage.setReplyMarkup(markup);
            return sendMessage;
        }
       return sendMessage1;
    }

    public static SendMessage getEditedInfo(Update update, String editType) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        String text = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (editType.equals(BotState.CHANGE_NAME)){
            user.setName(text);
            sendMessage.setText(user.getLang().equals("UZ")?
                    "Ism ozgartirildi."
                            :"Name was changed"
                    );
        }
        else if (editType.equals(BotState.CHANGE_PHONE)){
            user.setPhoneNumber(text);
            sendMessage.setText(user.getLang().equals("UZ")?
                    "Telefon raqam ozgartirildi."
                    :"PhoneNumber was changed"
            );
        }
        else{
            user.setAddress(text);
            sendMessage.setText(user.getLang().equals("UZ")?
                    "Address ozgartirildi."
                    :"Address was changed"
            );
        }
        changeUserInfo(user);
        return sendMessage;
    }

    public static SendMessage editLocation(Update update) {
        long chatId = getChatId(update);
        TgUser user = getUserFromListByChatId(chatId);
        Location location = update.getMessage().getLocation();
        user.setLon(location.getLongitude());
        user.setLat(location.getLatitude());
        changeUserInfo(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(user.getLang().equals("UZ")?
                "Location ozgartirildi"
                        :"Location was changed"
                );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
        return sendMessage;
    }


}
