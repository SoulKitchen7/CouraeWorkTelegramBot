package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.component.SendHelper;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final String welcomeMessage = "Привет! Я помогу тебе напомнить о запланированном событии! " +
            "Отправь сообщение в формате: 01.01.2023 00:00 Текст события";
    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private  final SendHelper sendHelper;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService,
                                      SendHelper sendHelper) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.sendHelper = sendHelper;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }


    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                String text = update.message().text();
                Long chatId = update.message().chat().id();

                if ("/start".equals(text)) {
                    sendHelper.sendMessage (chatId, welcomeMessage);
                } else {
                    Matcher matcher = PATTERN.matcher(text);
                    LocalDateTime localDateTime;

                    if (matcher.find() &&  (localDateTime = parse(matcher.group(1))) !=null) {
                        String message = matcher.group(3);
                        notificationTaskService.create(chatId, message, localDateTime);
                        sendHelper.sendMessage(chatId, "Событие запланировано.");
                    } else {
                        sendHelper.sendMessage(chatId, "Некорректный формат сообщения!");
                    }
                }
            });
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String localDateTime){
        try {
            return LocalDateTime.parse(localDateTime, FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

