package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;



@Entity
@DiscriminatorValue("shared")
public class Shared extends Task {

    private LocalDateTime dateOnFeed;
    private String userGuidance;
    @OneToMany
    private ArrayList<Comment> comments;

    public Shared() {
    }

    public Shared(String name, Topic topic, TaskState state, @Nullable LocalDateTime deadline,
                  String description, int percentageOfCompletion, int complexity, int priority,
                  Timetable timeTable, int totalTime, DefaultStrategy strategy, ArrayList<Resource> resources) {
        super(name, complexity, description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategy, resources);
        Feed.getInstance().addTask(this);

    }

    public LocalDateTime getDateOnFeed() {
        return dateOnFeed;
    }

    public void setDateOnFeed(LocalDateTime dateOnFeed) {
        this.dateOnFeed = dateOnFeed;
    }

    public String getUserGuidance() {
        return userGuidance;
    }

    public void updateUserGuidance(String text) {
        this.userGuidance = text;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void bestComment(Comment comment, User owner) {
        if (owner.getTasks().contains(this)) {
            comment.setIsBest(true);
            comment.getAuthor().incrementTopicScore(this.getTopic());
        }
    }

    @Override
    public void toCalendar(User user) {
        commonToCalendarLogic(user);
    } // LA GESTIONE DEL CAMPO USERGUIDANCE È AFFIDATA A ENDPPOINT E SERVICE (vedi *1)

    @Override
    public void handleLimitExceeded(User user) {
        // Rimuovo il task dal calendario, sposto il task dalla cartella InProgress a quella Freezed e lo rimuovo dal feed
        removeAndFreezeTask(user, this);
        Feed.getInstance().getShared().remove(this);
    }

    @Override
    public void deleteTask(User user) {
        user.getCalendar().removeSessions(this);
        ArrayList<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }

            }
        }
        Feed.getInstance().getShared().remove(this);
    }

    @Override
    public void modifyTask(User user) {
        commonModifyLogic(user);
        Feed.getInstance().getShared().remove(this);
    }

    @Override
    public void completeTaskBySessions(User user) {
        commonCompleteBySessionsLogic(user);
        Feed.getInstance().getShared().remove(this);
    }

    public void completeBySessionsAndChooseBestComment(User user, Comment comment) {
        if (comments.contains(comment)) {
            commonCompleteBySessionsLogic(user);
            Feed.getInstance().getShared().remove(this);
            bestComment(comment, user);
        }
    }

    @Override
    public void forcedCompletion(User user) {
        commonForcedCompletionLogic(user);
        Feed.getInstance().getShared().remove(this);
    }

    public void removeTaskJustFromFeed(User user) {
        // Rimuovi il task dal feed
        Feed.getInstance().getShared().remove(this);

        ArrayList<Folder> folders = user.getFolders();
        boolean taskRemovedFromShared = false;

        for (Folder folder : folders) {
            // Rimuove il task solo se è nella cartella SHARED
            if (folder.getFolderType() == FolderType.SHARED && !taskRemovedFromShared) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        taskRemovedFromShared = true; // Indica che è stato rimosso da SHARED
                        break;
                    }
                }
            }

            // Aggiunge il task alla cartella PERSONAL se è stato rimosso da SHARED
            if (folder.getFolderType() == FolderType.PERSONAL && taskRemovedFromShared) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (subfolder.getType() == SubfolderType.INPROGRESS) {
                        subfolder.getTasks().add(this);
                        break; // Aggiunto, non serve continuare
                    }
                }
            }
        }
    }

    
}









//*1
//@RestController
//@RequestMapping("/tasks")
//public class TaskEndpoint {
//
//    @Autowired
//    private TaskService taskService;
//
//    @PostMapping("/{taskId}/toCalendar")
//    public ResponseEntity<String> addToCalendar(
//            @PathVariable Long taskId,
//            @RequestParam String guidance,
//            @RequestBody User user) {
//        try {
//            taskService.addTaskToCalendar(taskId, guidance, user);
//            return ResponseEntity.ok("Task added to calendar successfully.");
//        } catch (UnsupportedOperationException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//}
//@Service
//public class TaskService {
//
//    public void addTaskToCalendar(Long taskId, String guidance, User user) {
//        Task task = findById(taskId);
//        if (task instanceof Shared) {
//            ((Shared) task).updateUserGuidance(guidance);
//            ((Shared) task).toCalendar(new Calendar(), user);
//        } else {
//            throw new UnsupportedOperationException("Task type not supported for this operation.");
//        }
//    }
//
//    public Task findById(Long taskId) {
//        // Logica per trovare il task nel repository
//    }
//}

