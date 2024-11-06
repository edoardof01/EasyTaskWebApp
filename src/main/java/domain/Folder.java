package domain;

import jakarta.persistence.*;

import java.util.ArrayList;


@Entity
public class Folder {
    @Id
    @Column( nullable = false)
    private Long id;
    @Enumerated(EnumType.STRING)
    private FolderType folderType;
    @ManyToOne
    private User user;
    @OneToMany
    private final ArrayList<Subfolder> subfolders = new ArrayList<>();

    public Folder() {}

    public Folder(FolderType folderType, User user) {
      this.folderType = folderType;

      this.subfolders.add(new Subfolder(SubfolderType.TODO));
      this.subfolders.add(new Subfolder(SubfolderType.INPROGRESS));
      this.subfolders.add(new Subfolder(SubfolderType.FREEZED));
      this.subfolders.add(new Subfolder(SubfolderType.FINISHED));
    }
    public Long getId() {
      return id;
    }
    public FolderType getFolderType() {
      return folderType;
    }
    public void setFolderType(FolderType folderType) {
      this.folderType = folderType;
    }
    public User getUser() {
      return user;
    }
    public void setUser(User user) {
      this.user = user;
    }
    public ArrayList<Subfolder> getSubfolders() {
        return subfolders;
    }
}