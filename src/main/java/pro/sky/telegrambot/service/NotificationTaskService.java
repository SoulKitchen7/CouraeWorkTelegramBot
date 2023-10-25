package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }
    @Transactional
    public void create (Long chatId,
                        String message,
                        LocalDateTime localDateTime) {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setUserId(chatId);
        notificationTask.setMessage(message);
        notificationTask.setNotificationDateTime(localDateTime.truncatedTo(ChronoUnit.MINUTES));
        notificationTaskRepository.save(notificationTask);
    }
}
