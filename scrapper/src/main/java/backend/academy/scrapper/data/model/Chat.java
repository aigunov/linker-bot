package backend.academy.scrapper.data.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "chat")
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "tg_id")
    private Long tgId;

    @NotNull
    @Column(name = "nickname")
    private String nickname;

    @OneToMany(
            mappedBy = "chat",
            cascade = {CascadeType.REMOVE})
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(
            mappedBy = "chat",
            cascade = {CascadeType.REMOVE})
    private Set<Filter> filters = new HashSet<>();

    @ManyToMany(mappedBy = "chats")
    private Set<Link> links = new HashSet<>();
}
