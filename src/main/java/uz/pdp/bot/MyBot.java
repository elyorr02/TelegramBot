package uz.pdp.bot;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MyBot extends TelegramLongPollingBot {
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        long chatId = MyBotService.getChatId(update);
        TgUser user = MyBotService.getUserFromListByChatId(chatId);
        if (update.hasMessage()){
            if (update.getMessage().hasContact()){
                execute(MyBotService.getContact(update));
            }
            else if (update.getMessage().hasLocation()){
                if (user.getBotState().equals(BotState.CHANGE_LOCATION)){
                    execute(MyBotService.editLocation(update));
                }else {
                    execute(MyBotService.getLocation(update));
                }
            }
            else {
                String text = update.getMessage().getText();
                if (text.equals("/start")){
                    execute(MyBotService.getStart(update));
                }else {
                    if (user.getBotState().equals(BotState.CHOOSE_LANG)){
                        execute(MyBotService.getLang(update));
                    }
                    else if (user.getBotState().equals(BotState.SHARE_LOCATION)){
                        execute(MyBotService.getLocation(update));
                    }
                    else if (user.getBotState().equals(BotState.SHARE_NAME)){
                        if (text.equals("HA")||text.equals("YES")){
                            Message executeMessage = execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText(".").setReplyMarkup(new ReplyKeyboardRemove()));
                            execute(new DeleteMessage().setChatId(chatId).setMessageId(executeMessage.getMessageId()));
                            execute(MyBotService.getNameByButton(update));
                        }
                        else if (text.equals("YO'Q")||text.equals("NO")){
                            execute(MyBotService.askEnterName(update));
                        }else {

                        }
                    }
                    else if (user.getBotState().equals(BotState.ENTER_NAME)){
                        execute(MyBotService.getNameByEnter(update));
                    }
                    else if (user.getBotState().equals(BotState.CHANGE_NAME)){
                        execute(MyBotService.getEditedInfo(update,BotState.CHANGE_NAME));
                    }
                    else if (user.getBotState().equals(BotState.CHANGE_PHONE)){
                        execute(MyBotService.getEditedInfo(update,BotState.CHANGE_PHONE));
                    }
                    else if (user.getBotState().equals(BotState.CHANGE_LOCATION)){
                        execute(MyBotService.getEditedInfo(update,BotState.CHANGE_LOCATION));
                    }
                }
            }

        }
        else if (update.getCallbackQuery()!=null){
            String data = update.getCallbackQuery().getData();
            if (data.equals(BotState.SEE_PRODUCT)){
                for (Product product : MyBotService.productList) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setCaption(product.getName()+"\n"+
                            product.getDescription()+"\n"+
                            product.getPrice()+"\n"+
                            "\uD83D\uDE9B "+(user.getLang().equals("UZ")?"Tekin":"Free")
                            );
                    File file = new File(product.getPhotoUrl());
                    sendPhoto.setPhoto(product.getName()+" photo",new FileInputStream(file));
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowList=new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();

                    InlineKeyboardButton button=new InlineKeyboardButton();
                    button.setText(user.getLang().equals("UZ")?
                            "Buyurtma berish"
                            :"Booking");
                    button.setCallbackData("productId:"+product.getId());
                    row.add(button);
                    rowList.add(row);
                    markup.setKeyboard(rowList);
                    sendPhoto.setReplyMarkup(markup);
                    execute(sendPhoto);
                }
                execute(MyBotService.showProduct(update));
            }
            else if (data.startsWith("productId:")){
                execute(MyBotService.getProduct(update));
            }
            else if (data.equals(BotState.GET_CURRENCY_RATE)){
                execute(MyBotService.getUSDRate(update));
            }
            else if (data.equals(BotState.EDIT)){
                execute(MyBotService.showEditMenu(update));
            }
            else if (data.equals(BotState.CHANGE_NAME)
            ||data.equals(BotState.CHANGE_PHONE)
                    ||
                    data.equals(BotState.CHANGE_LOCATION)
            ){
                execute(MyBotService.change(update));
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "G50MentorBot";
    }

    @Override
    public String getBotToken() {
        return "1896120943:AAHWBHdBa7Lt-RAugfFFHkNwpUbKhbnqJ0Q";
    }
}
